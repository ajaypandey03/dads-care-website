package com.dadscare.backend.telemetry;

import com.dadscare.backend.alert.RulesEngineService;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.DeviceRepository;
import com.dadscare.backend.unlock.UnlockRequestService;
import com.dadscare.backend.velosyss.VelosyssWebhookEvent;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies one validated {@link VelosyssWebhookEvent} (§4.2 of the Integration Guide):
 * de-dupes by {@code eventId} (delivery is at-least-once, §4.4), resolves the target
 * {@link com.dadscare.backend.tenant.Organization} via the device's {@code terminalId}
 * (not from {@code integrationCode} — that identifies Velosyss's view of Dad's Care as a
 * whole, not which of Dad's Care's own customers owns the device), and dispatches on
 * {@code eventType}:
 * <ul>
 *   <li>{@code SEAL_STATE} → a shutter open/close {@link RawEvent}, fed to {@link RulesEngineService#evaluate}
 *   <li>{@code ALARM} → a always-notify {@link RawEvent}, fed to {@link RulesEngineService#evaluateAlarm}
 *   <li>{@code COMMAND_RESULT} → applied to the originating {@link com.dadscare.backend.unlock.UnlockRequest}
 *       via {@link UnlockRequestService#applyCommandResult}, and recorded as a RawEvent for audit only
 *       (never itself produces an Alert)
 * </ul>
 * This same method is reused by {@code VelosyssPollingService} to replay events fetched
 * from {@code GET /locks/events} during reconciliation — the eventId dedup makes that safe
 * even when a webhook for the same event already landed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    /** Window to look for a co-occurring ALARM when scoring a SEAL_STATE open — see RawEvent#tamperFlag. */
    private static final Duration ALARM_COOCCURRENCE_WINDOW = Duration.ofMinutes(5);

    private final DeviceRepository deviceRepository;
    private final RawEventRepository rawEventRepository;
    private final RulesEngineService rulesEngineService;
    private final UnlockRequestService unlockRequestService;

    @Transactional
    public IngestOutcome ingest(VelosyssWebhookEvent event) {
        if (rawEventRepository.existsByVelosyssEventId(event.eventId())) {
            return IngestOutcome.DUPLICATE;
        }

        Device device = deviceRepository.findByVelosyssTerminalId(event.terminalId()).orElse(null);
        if (device == null) {
            // A terminal Velosyss knows about that we haven't provisioned/assigned to an
            // Organization yet — log and accept-but-drop rather than reject the delivery.
            log.warn(
                    "Webhook event for unrecognized terminalId={}, eventId={}, eventType={} — skipped",
                    event.terminalId(),
                    event.eventId(),
                    event.eventType());
            return IngestOutcome.UNKNOWN_DEVICE;
        }

        switch (event.eventType()) {
            case ALARM -> handleAlarm(device, event);
            case SEAL_STATE -> handleSealState(device, event);
            case COMMAND_RESULT -> handleCommandResult(device, event);
        }
        return IngestOutcome.ACCEPTED;
    }

    private void handleAlarm(Device device, VelosyssWebhookEvent event) {
        RawEvent raw = newRawEvent(device, event, RawEvent.EventType.ALARM);
        raw.setAlarmCode(event.alarm());
        raw.setAlarmDescription(event.description());
        rawEventRepository.save(raw);
        markOnline(device);
        rulesEngineService.evaluateAlarm(raw);
    }

    private void handleSealState(Device device, VelosyssWebhookEvent event) {
        // Only fires on a real transition (§4.2) — sealed=false is an unseal (shutter can
        // now open), sealed=true a seal.
        RawEvent.EventType direction =
                Boolean.TRUE.equals(event.sealed()) ? RawEvent.EventType.LOCK_CLOSE : RawEvent.EventType.LOCK_OPEN;

        RawEvent raw = newRawEvent(device, event, direction);
        raw.setSealed(event.sealed());
        raw.setShackleClosed(event.shackleClosed());
        raw.setTamperFlag(hadRecentAlarm(device, raw.getEventTimestamp()));
        rawEventRepository.save(raw);

        device.setLastSealed(event.sealed());
        device.setLastShackleClosed(event.shackleClosed());
        markOnline(device);

        rulesEngineService.evaluate(raw);
    }

    private void handleCommandResult(Device device, VelosyssWebhookEvent event) {
        RawEvent raw = newRawEvent(device, event, RawEvent.EventType.COMMAND_RESULT);
        raw.setCommandRequestId(event.requestId());
        raw.setCommandAction(event.action());
        raw.setCommandStatus(event.status());
        raw.setCommandSucceeded(event.succeeded());
        raw.setCommandMessage(event.message());
        rawEventRepository.save(raw);
        markOnline(device);

        unlockRequestService.applyCommandResult(event);
    }

    private boolean hadRecentAlarm(Device device, Instant around) {
        return rawEventRepository.existsByDeviceIdAndEventTypeAndEventTimestampBetween(
                device.getId(),
                RawEvent.EventType.ALARM,
                around.minus(ALARM_COOCCURRENCE_WINDOW),
                around.plus(ALARM_COOCCURRENCE_WINDOW));
    }

    private RawEvent newRawEvent(Device device, VelosyssWebhookEvent event, RawEvent.EventType eventType) {
        RawEvent raw = new RawEvent();
        raw.setOrganization(device.getOrganization());
        raw.setDevice(device);
        raw.setVelosyssEventId(event.eventId());
        raw.setEventType(eventType);
        return raw;
    }

    private void markOnline(Device device) {
        device.setLastSeenAt(Instant.now());
        device.setOnline(true);
        deviceRepository.save(device);
    }

    public enum IngestOutcome {
        ACCEPTED,
        DUPLICATE,
        UNKNOWN_DEVICE
    }
}
