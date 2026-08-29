package com.dadscare.backend.unlock;

import com.dadscare.backend.forms.GodownFormService;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.DeviceRepository;
import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import com.dadscare.backend.velosyss.VelosyssCommandClient;
import com.dadscare.backend.velosyss.VelosyssReadClient;
import com.dadscare.backend.velosyss.VelosyssWebhookEvent;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates and relays {@link UnlockRequest}s, persisting any submitted godown form data
 * (stock/trucks/labor/custom fields — see {@link GodownFormService}) in the same
 * transaction. The request is persisted <em>before</em> the Velosyss call, so even a
 * failed relay leaves an audit trail, and the record {@link
 * com.dadscare.backend.alert.RulesEngineService} needs already exists the moment the
 * command is (successfully) sent — never reconstructed after the telemetry arrives.
 *
 * <p>Also applies {@code COMMAND_RESULT} webhook outcomes (see {@code WebhookService})
 * back onto the originating request via {@link #applyCommandResult}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UnlockRequestService {

    private final UnlockRequestRepository unlockRequestRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final VelosyssCommandClient velosyssCommandClient;
    private final GodownFormService godownFormService;

    @Transactional
    public UnlockRequestDto create(Long deviceId, CreateUnlockRequestRequest request) {
        Long organizationId = TenantContext.organizationId();

        Device device = deviceRepository
                .findByIdAndOrganizationId(deviceId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Device " + deviceId + " not found"));

        User requester = userRepository
                .findByIdAndOrganizationId(TenantContext.userId(), organizationId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));

        UnlockRequest unlockRequest = new UnlockRequest();
        unlockRequest.setOrganization(device.getOrganization());
        unlockRequest.setDevice(device);
        unlockRequest.setRequestedBy(requester);
        unlockRequest.setCommandType(request.commandType());
        unlockRequest.setStatus(UnlockRequestStatus.PENDING);
        unlockRequestRepository.save(unlockRequest);

        godownFormService.createIfPresent(unlockRequest, device.getOrganization(), request);

        relay(unlockRequest, device, requester);

        return UnlockRequestDto.from(unlockRequest);
    }

    private void relay(UnlockRequest unlockRequest, Device device, User requester) {
        VelosyssCommandClient.CommandResult result =
                velosyssCommandClient.issueCommand(device.getVelosyssDeviceRef(), unlockRequest.getCommandType(), requester.getName());

        if (result.accepted()) {
            VelosyssCommandClient.CommandResponseBody body = result.response();
            unlockRequest.setVelosyssRequestId(body.requestId());
            unlockRequest.setRelayedAt(Instant.now());
            unlockRequest.setDispatchedAt(VelosyssReadClient.toInstantUtc(body.dispatchedAt()));
            unlockRequest.setMessage(body.message());
            unlockRequest.setStatus(mapVelosyssStatus(body.status()));
            if (unlockRequest.getStatus() == UnlockRequestStatus.RESPONDED) {
                unlockRequest.setSucceeded(body.responseSucceeded());
                unlockRequest.setRespondedAt(VelosyssReadClient.toInstantUtc(body.respondedAt()));
            }
        } else {
            unlockRequest.setStatus(UnlockRequestStatus.FAILED);
            unlockRequest.setFailureReason(result.failureReason());
        }
    }

    /**
     * Applies a {@code COMMAND_RESULT} webhook event (§4.2) to the request Velosyss's
     * {@code requestId} identifies. A no-match is logged, not an error — the command
     * could have been issued outside this backend (e.g. directly from Velosyss's own
     * console), which is a legitimate case Authorized-Open Correlation is designed to
     * flag rather than choke on.
     */
    @Transactional
    public void applyCommandResult(VelosyssWebhookEvent event) {
        unlockRequestRepository.findByVelosyssRequestId(event.requestId()).ifPresentOrElse(
                unlockRequest -> {
                    UnlockRequestStatus status = mapVelosyssStatus(event.status());
                    unlockRequest.setStatus(status);
                    unlockRequest.setSucceeded(event.succeeded());
                    unlockRequest.setMessage(event.message());
                    if (status == UnlockRequestStatus.EXPIRED) {
                        unlockRequest.setExpiredAt(Instant.now());
                    } else {
                        unlockRequest.setRespondedAt(Instant.now());
                    }
                    unlockRequestRepository.save(unlockRequest);
                },
                () -> log.info(
                        "COMMAND_RESULT for unknown requestId={} (terminalId={}) — no matching UnlockRequest, "
                                + "likely issued outside dadscare-backend",
                        event.requestId(),
                        event.terminalId()));
    }

    /**
     * Fallback reconciliation for when Velosyss's {@code COMMAND_RESULT} webhook/event
     * never arrives (observed in practice against the real API — its {@code GET
     * /locks/events} response shape doesn't actually carry {@code terminalId} or a
     * {@code COMMAND_RESULT}-shaped payload the way the Integration Guide describes, so
     * {@link #applyCommandResult} never fires). {@code GET /locks/positions} — polled
     * separately, unconditionally, every {@code app.velosyss.positions-poll-ms} — has
     * proven reliable, so: if a device's observed {@code sealed} state now matches what an
     * outstanding request was asking for, treat that as the terminal outcome. Less precise
     * than a real webhook (no exact response timestamp, no Velosyss-side failure detail),
     * but keeps the UI from hanging on QUEUED forever when the lock has, physically,
     * already done what was asked.
     */
    @Transactional
    public void reconcileFromObservedSealState(Long deviceId, Boolean sealed) {
        if (sealed == null) {
            return;
        }
        List<UnlockRequest> outstanding = unlockRequestRepository.findAllByDeviceIdAndStatusIn(
                deviceId, List.of(UnlockRequestStatus.PENDING, UnlockRequestStatus.QUEUED, UnlockRequestStatus.DISPATCHED));
        for (UnlockRequest request : outstanding) {
            boolean matchesObservedState = (request.getCommandType() == CommandType.UNLOCK && !sealed)
                    || (request.getCommandType() == CommandType.LOCK && sealed);
            if (!matchesObservedState) {
                continue;
            }
            request.setStatus(UnlockRequestStatus.RESPONDED);
            request.setSucceeded(true);
            request.setRespondedAt(Instant.now());
            request.setMessage("Reconciled from Velosyss positions poll — no COMMAND_RESULT event was received.");
            unlockRequestRepository.save(request);
        }
    }

    /**
     * Marks any request that's been outstanding longer than {@code maxAge} as EXPIRED —
     * a request the lock never actually executes (dispatched into a stale/dead session,
     * genuinely offline, etc.) has no observed state change for {@link
     * #reconcileFromObservedSealState} to ever match against, so without this it would sit
     * QUEUED/DISPATCHED forever. Velosyss's own command timeout has been observed at
     * roughly 60-90s in practice; {@code maxAge} should be comfortably longer than that.
     */
    @Transactional
    public void expireStaleRequests(java.time.Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        List<UnlockRequest> stale = unlockRequestRepository.findAllByStatusInAndCreatedAtBefore(
                List.of(UnlockRequestStatus.PENDING, UnlockRequestStatus.QUEUED, UnlockRequestStatus.DISPATCHED),
                cutoff);
        for (UnlockRequest request : stale) {
            request.setStatus(UnlockRequestStatus.EXPIRED);
            request.setExpiredAt(Instant.now());
            request.setMessage("No response from the lock within the timeout window.");
            unlockRequestRepository.save(request);
        }
    }

    private UnlockRequestStatus mapVelosyssStatus(String velosyssStatus) {
        if (velosyssStatus == null) {
            return UnlockRequestStatus.QUEUED;
        }
        try {
            return UnlockRequestStatus.valueOf(velosyssStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized Velosyss command status \"{}\" — treating as QUEUED", velosyssStatus);
            return UnlockRequestStatus.QUEUED;
        }
    }

    @Transactional(readOnly = true)
    public List<UnlockRequestDto> listForOrganization() {
        return unlockRequestRepository.findAllByOrganizationIdOrderByCreatedAtDesc(TenantContext.organizationId())
                .stream()
                .map(UnlockRequestDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnlockRequestDto get(Long id) {
        return unlockRequestRepository
                .findByIdAndOrganizationId(id, TenantContext.organizationId())
                .map(UnlockRequestDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Unlock request " + id + " not found"));
    }
}
