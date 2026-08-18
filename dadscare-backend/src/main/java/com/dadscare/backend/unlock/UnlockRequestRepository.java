package com.dadscare.backend.unlock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UnlockRequestRepository extends JpaRepository<UnlockRequest, Long> {

    Optional<UnlockRequest> findByIdAndOrganizationId(Long id, Long organizationId);

    List<UnlockRequest> findAllByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    /**
     * Candidates for Authorized-Open Correlation: successfully-relayed requests of the
     * given command type, for the given device, whose {@code relayedAt} falls within the
     * correlation window around the telemetry event's timestamp. Ordered so the closest
     * match in time is picked first if more than one candidate exists.
     */
    @Query(
            """
            select ur from UnlockRequest ur
            where ur.device.id = :deviceId
              and ur.commandType = :commandType
              and ur.status = com.dadscare.backend.unlock.UnlockRequestStatus.RELAYED
              and ur.relayedAt between :windowStart and :windowEnd
            order by ur.relayedAt desc
            """)
    List<UnlockRequest> findCorrelationCandidates(
            @Param("deviceId") Long deviceId,
            @Param("commandType") CommandType commandType,
            @Param("windowStart") Instant windowStart,
            @Param("windowEnd") Instant windowEnd);
}
