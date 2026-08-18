package com.dadscare.backend.telemetry;

import com.dadscare.backend.alert.RulesEngineService;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.site.DeviceRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies a validated {@link LockEventBatch} from Velosyss: de-dupes by
 * {@code eventId}, resolves each event's target {@link com.dadscare.backend.tenant.Organization}
 * via its device (see {@link DeviceRepository#findByVelosyssDeviceRef}, not from the
 * payload's {@code tenantRef} — that identifies Velosyss's view of "Dad's Care" as a
 * whole, not which of Dad's Care's own customers owns the device), and persists each
 * event as an immutable {@link RawEvent}.
 *
 * <p>Once the whole batch is persisted, every {@code LOCK_OPEN}/{@code LOCK_CLOSE} event
 * is handed to {@link RulesEngineService} — deliberately after the full batch lands, not
 * per-event mid-loop, so its quick-reclose check can see a LOCK_CLOSE that arrived just
 * after its LOCK_OPEN in the same push.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final DeviceRepository deviceRepository;
    private final RawEventRepository rawEventRepository;
    private final RulesEngineService rulesEngineService;

    @Transactional
    public IngestResult ingest(LockEventBatch batch) {
        int accepted = 0;
        int duplicates = 0;
        int unknownDevices = 0;
        List<RawEvent> lockStateEvents = new ArrayList<>();

        for (LockEventPayload event : batch.events()) {
            if (rawEventRepository.existsByVelosyssEventId(event.eventId())) {
                duplicates++;
                continue;
            }

            Device device = deviceRepository.findByVelosyssDeviceRef(event.deviceRef()).orElse(null);
            if (device == null) {
                // A device Velosyss knows about that we haven't provisioned/assigned to an
                // Organization yet — log and skip rather than fail the whole batch.
                log.warn("Webhook event for unrecognized deviceRef={}, eventId={} — skipped",
                        event.deviceRef(), event.eventId());
                unknownDevices++;
                continue;
            }

            RawEvent raw = persist(device, event);
            updateDeviceLiveState(device, event);
            accepted++;

            if (raw.getEventType() == RawEvent.EventType.LOCK_OPEN
                    || raw.getEventType() == RawEvent.EventType.LOCK_CLOSE) {
                lockStateEvents.add(raw);
            }
        }

        for (RawEvent event : lockStateEvents) {
            rulesEngineService.evaluate(event);
        }

        return new IngestResult(accepted, duplicates, unknownDevices);
    }

    private RawEvent persist(Device device, LockEventPayload event) {
        RawEvent raw = new RawEvent();
        raw.setOrganization(device.getOrganization());
        raw.setDevice(device);
        raw.setVelosyssEventId(event.eventId());
        raw.setEventType(RawEvent.EventType.valueOf(event.eventType().name()));
        raw.setLockStatus(event.lockStatus());
        raw.setLatitude(event.lat());
        raw.setLongitude(event.lng());
        raw.setSpeed(event.speed());
        raw.setBatteryPct(event.battery());
        raw.setMotionMagnitude(event.motionMagnitude());
        raw.setTamperFlag(event.tamperFlag());
        raw.setSourceSensor(event.sourceSensor());
        raw.setEventTimestamp(event.eventTimestamp());
        raw.setReceivedAt(Instant.now());
        return rawEventRepository.save(raw);
    }

    private void updateDeviceLiveState(Device device, LockEventPayload event) {
        // Receiving any event at all — including a heartbeat — means the device is online.
        device.setLastSeenAt(event.eventTimestamp());
        device.setOnline(true);
        if (event.battery() != null) {
            device.setLastBatteryPct(event.battery());
        }
        deviceRepository.save(device);
    }

    public record IngestResult(int accepted, int duplicates, int unknownDevices) {
    }
}
