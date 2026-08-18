package com.dadscare.backend.unlock;

import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.DeviceRepository;
import com.dadscare.backend.tenant.TenantContext;
import com.dadscare.backend.user.User;
import com.dadscare.backend.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates and relays {@link UnlockRequest}s. On submit, the app calls this with a
 * (Godown form is handled entirely client-side in Phase 4 — this service only owns the
 * lock-control half). The request is persisted <em>before</em> the Velosyss call, so even
 * a failed relay leaves an audit trail, and the record {@link
 * com.dadscare.backend.alert.RulesEngineService} needs already exists the moment the
 * command is (successfully) sent — never reconstructed after the telemetry arrives.
 */
@Service
@RequiredArgsConstructor
public class UnlockRequestService {

    private final UnlockRequestRepository unlockRequestRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final VelosyssCommandClient velosyssCommandClient;

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
        unlockRequest.setVelosyssRequestId(UUID.randomUUID().toString());
        unlockRequest.setStatus(UnlockRequestStatus.PENDING);
        unlockRequestRepository.save(unlockRequest);

        relay(unlockRequest, device);

        return UnlockRequestDto.from(unlockRequest);
    }

    private void relay(UnlockRequest unlockRequest, Device device) {
        VelosyssCommandClient.CommandResult result = velosyssCommandClient.issueCommand(
                device.getVelosyssDeviceRef(), unlockRequest.getCommandType(), unlockRequest.getVelosyssRequestId());

        if (result.accepted()) {
            unlockRequest.setStatus(UnlockRequestStatus.RELAYED);
            unlockRequest.setRelayedAt(Instant.now());
        } else {
            unlockRequest.setStatus(UnlockRequestStatus.FAILED);
            unlockRequest.setFailureReason(result.failureReason());
        }
    }

    @Transactional(readOnly = true)
    public List<UnlockRequestDto> listForOrganization() {
        return unlockRequestRepository.findAllByOrganizationIdOrderByCreatedAtDesc(TenantContext.organizationId())
                .stream()
                .map(UnlockRequestDto::from)
                .toList();
    }
}
