package com.dadscare.backend.velosyss;

import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.DeviceRepository;
import com.dadscare.backend.telemetry.WebhookService;
import com.dadscare.backend.unlock.UnlockRequestService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * §5 of the Integration Guide describes polling as "a reconciliation safety net, not your
 * primary signal" — real-time events were meant to arrive via the webhook (see
 * WebhookController). In practice, against the real Velosyss API, neither the webhook nor
 * {@link #pollEvents()}'s {@code GET /locks/events} replay has ever actually delivered a
 * usable {@code SEAL_STATE}/{@code COMMAND_RESULT} event (see this class's git history/
 * commit messages) — the events endpoint's real response shape doesn't match the
 * documented contract at all. {@link #pollPositions()}'s {@code GET /locks/positions} is
 * the only signal that has proven reliable, so despite the original design intent, it is
 * now the primary signal in this deployment: on top of refreshing each {@link Device}'s
 * online/battery/position cache, it detects an actual {@code sealed} transition and
 * synthesizes the same {@code SEAL_STATE} event a real webhook would have delivered,
 * through the exact same {@link WebhookService#ingest} path — so RawEvent history, Alert
 * classification, and WhatsApp/push notifications all still fire correctly from this
 * signal. {@link #pollEvents()} is kept as a genuine backstop in case Velosyss's events
 * feed ever does start matching the documented shape.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VelosyssPollingService {

    /** How far back to start reconciling on the very first run (before any cursor exists). */
    private static final long INITIAL_LOOKBACK_MINUTES = 60;

    private static final int EVENTS_PAGE_LIMIT = 200;

    private final VelosyssReadClient velosyssReadClient;
    private final DeviceRepository deviceRepository;
    private final WebhookService webhookService;
    private final IntegrationCursorRepository integrationCursorRepository;
    private final UnlockRequestService unlockRequestService;

    @Scheduled(fixedDelayString = "${app.velosyss.positions-poll-ms:20000}", initialDelayString = "${app.velosyss.positions-poll-ms:20000}")
    @Transactional
    public void pollPositions() {
        if (!velosyssReadClient.isConfigured()) {
            return;
        }
        List<VelosyssReadClient.PositionDto> positions = velosyssReadClient.getPositions();
        if (positions.isEmpty()) {
            return;
        }
        Map<String, Device> devicesByTerminalId = deviceRepository.findAllByVelosyssTerminalIdIsNotNull().stream()
                .collect(Collectors.toMap(Device::getVelosyssTerminalId, Function.identity()));

        for (VelosyssReadClient.PositionDto position : positions) {
            Device device = devicesByTerminalId.get(position.terminalId());
            if (device == null) {
                continue; // Not (yet) mapped to a Dad's Care device — nothing to reconcile.
            }
            Boolean previousSealed = device.getLastSealed();

            device.setOnline(Boolean.TRUE.equals(position.isOnline()));
            if (position.lastDeviceTime() != null) {
                device.setLastSeenAt(VelosyssReadClient.toInstantUtc(position.lastDeviceTime()));
            }
            device.setLastLatitude(position.latitude());
            device.setLastLongitude(position.longitude());
            device.setLastSealed(position.sealed());
            device.setLastShackleClosed(position.shackleClosed());
            if (position.batteryVoltageMv() != null) {
                device.setLastBatteryMv(position.batteryVoltageMv());
                device.setLastBatteryPct(approximateBatteryPercent(position.batteryVoltageMv()));
            }
            // Fallback for when Velosyss's COMMAND_RESULT never arrives (webhook or events
            // poll) — see UnlockRequestService#reconcileFromObservedSealState's own comment.
            unlockRequestService.reconcileFromObservedSealState(device.getId(), position.sealed());

            emitSyntheticSealStateOnTransition(device, previousSealed, position);
        }
        deviceRepository.saveAll(devicesByTerminalId.values());
    }

    /**
     * A real sealed-state change observed only via the positions poll would otherwise
     * never produce a RawEvent/Alert/notification at all (see this class's own javadoc) —
     * this reconstructs the SEAL_STATE event a working webhook would have sent, so the
     * rest of the pipeline (RulesEngineService classification, NotificationDispatcher)
     * still fires. Skipped on the very first sighting of a device ({@code previousSealed
     * == null}) — that's not a transition, just this poll cycle learning its initial
     * state, and treating it as one would fire a spurious alert every time the app
     * restarts.
     */
    private void emitSyntheticSealStateOnTransition(
            Device device, Boolean previousSealed, VelosyssReadClient.PositionDto position) {
        if (previousSealed == null || position.sealed() == null || previousSealed.equals(position.sealed())) {
            return;
        }
        var event = new VelosyssWebhookEvent(
                "poll-" + device.getId() + "-" + System.currentTimeMillis(),
                VelosyssWebhookEvent.EventType.SEAL_STATE,
                null,
                device.getVelosyssTerminalId(),
                null,
                null,
                position.sealed(),
                position.shackleClosed(),
                null,
                null,
                null,
                null,
                null);
        webhookService.ingest(event);
    }

    /**
     * Runs independent of {@link VelosyssReadClient#isConfigured()} — even without a live
     * Velosyss connection, a request already sitting PENDING/QUEUED/DISPATCHED (e.g. from
     * before a misconfiguration was noticed) shouldn't stay open forever either. See
     * {@code UnlockRequestService#expireStaleRequests}'s own javadoc for why this exists
     * on top of {@link #pollPositions}'s state-change-based reconciliation.
     */
    @Scheduled(fixedDelayString = "${app.velosyss.positions-poll-ms:20000}", initialDelayString = "${app.velosyss.positions-poll-ms:20000}")
    public void expireStaleUnlockRequests() {
        unlockRequestService.expireStaleRequests(java.time.Duration.ofSeconds(150));
    }

    @Scheduled(fixedDelayString = "${app.velosyss.events-poll-ms:180000}", initialDelayString = "${app.velosyss.events-poll-ms:180000}")
    public void pollEvents() {
        if (!velosyssReadClient.isConfigured()) {
            return;
        }
        Instant since = currentCursor();
        Instant pollStartedAt = Instant.now();

        List<VelosyssWebhookEvent> events = velosyssReadClient.getEvents(since, EVENTS_PAGE_LIMIT);
        int replayed = 0;
        for (VelosyssWebhookEvent event : events) {
            WebhookService.IngestOutcome outcome = webhookService.ingest(event);
            if (outcome == WebhookService.IngestOutcome.ACCEPTED) {
                replayed++;
            }
        }
        if (!events.isEmpty()) {
            log.info(
                    "Velosyss events reconciliation: fetched={}, newlyAccepted={} (rest were already-seen duplicates)",
                    events.size(),
                    replayed);
        }

        // Advance to "now" (not the last event's own timestamp — the payload doesn't carry
        // one, see VelosyssWebhookEvent) minus a small safety margin, so a slow response
        // doesn't skip events that landed on Velosyss's side mid-poll.
        advanceCursor(pollStartedAt.minusSeconds(30));
    }

    /** Rough linear mapping (3200mV empty .. 4200mV full) — Velosyss's positions API gives raw voltage, not a percent. */
    private Integer approximateBatteryPercent(int millivolts) {
        int pct = (int) Math.round((millivolts - 3200) / 10.0);
        return Math.max(0, Math.min(100, pct));
    }

    // Not @Transactional — each call is a single repository operation, and SimpleJpaRepository's
    // save()/findBy* are already transactional on their own; an outer @Transactional here would
    // be a no-op anyway (self-invocation within this class bypasses the Spring proxy).
    private Instant currentCursor() {
        return integrationCursorRepository
                .findByCursorName(IntegrationCursor.EVENTS_CURSOR)
                .map(IntegrationCursor::getCursorValue)
                .orElseGet(() -> Instant.now().minusSeconds(INITIAL_LOOKBACK_MINUTES * 60));
    }

    private void advanceCursor(Instant value) {
        IntegrationCursor cursor = integrationCursorRepository
                .findByCursorName(IntegrationCursor.EVENTS_CURSOR)
                .orElseGet(() -> {
                    IntegrationCursor c = new IntegrationCursor();
                    c.setCursorName(IntegrationCursor.EVENTS_CURSOR);
                    return c;
                });
        cursor.setCursorValue(value);
        integrationCursorRepository.save(cursor);
    }
}
