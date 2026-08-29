package com.dadscare.backend.telemetry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawEventRepository extends JpaRepository<RawEvent, Long> {

    boolean existsByVelosyssEventId(String velosyssEventId);

    Optional<RawEvent> findByVelosyssEventId(String velosyssEventId);

    List<RawEvent> findAllByOrganizationIdOrderByEventTimestampDesc(Long organizationId);

    /** Used by RulesEngineService's quick-reclose check — deliberately narrow (one device, one type, one short window). */
    List<RawEvent> findAllByDeviceIdAndEventTypeAndEventTimestampBetween(
            Long deviceId, RawEvent.EventType eventType, Instant from, Instant to);

    /** Used to derive a shutter's last-known open/close state for the dashboard's live status view. */
    Optional<RawEvent> findFirstByDeviceIdAndEventTypeOrderByEventTimestampDesc(Long deviceId, RawEvent.EventType eventType);

    /** Used by WebhookService to decide RawEvent#tamperFlag: did an ALARM land on this device around the same time? */
    boolean existsByDeviceIdAndEventTypeAndEventTimestampBetween(
            Long deviceId, RawEvent.EventType eventType, Instant from, Instant to);
}
