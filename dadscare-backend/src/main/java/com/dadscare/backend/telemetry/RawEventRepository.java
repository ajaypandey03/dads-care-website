package com.dadscare.backend.telemetry;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawEventRepository extends JpaRepository<RawEvent, Long> {

    boolean existsByVelosyssEventId(String velosyssEventId);

    Optional<RawEvent> findByVelosyssEventId(String velosyssEventId);

    List<RawEvent> findAllByOrganizationIdOrderByEventTimestampDesc(Long organizationId);
}
