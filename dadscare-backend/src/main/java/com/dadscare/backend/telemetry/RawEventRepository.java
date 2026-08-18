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
}
