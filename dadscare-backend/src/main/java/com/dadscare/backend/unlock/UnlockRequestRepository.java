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

    /** Correlates an inbound {@code COMMAND_RESULT} webhook back to the request that caused it. */
    Optional<UnlockRequest> findByVelosyssRequestId(String velosyssRequestId);

    /**
     * Still-open requests for a device, used by {@code VelosyssPollingService}'s positions
     * poll to reconcile a request's outcome from observed lock state when Velosyss's
     * {@code COMMAND_RESULT} webhook/event never arrives — see
     * {@code UnlockRequestService#reconcileFromObservedSealState}.
     */
    List<UnlockRequest> findAllByDeviceIdAndStatusIn(Long deviceId, List<UnlockRequestStatus> statuses);

    /** Every still-open request across every device, for {@code UnlockRequestService#expireStaleRequests}. */
    List<UnlockRequest> findAllByStatusInAndCreatedAtBefore(List<UnlockRequestStatus> statuses, Instant cutoff);

    /**
     * Candidates for Authorized-Open Correlation: requests of the given command type, for
     * the given device, that Velosyss confirmed actually succeeded ({@code RESPONDED} +
     * {@code succeeded = true} — the real terminal-success state, per §6.4 of the
     * Integration Guide), whose {@code relayedAt} falls within the correlation window
     * around the telemetry event's timestamp. Ordered so the closest match in time is
     * picked first if more than one candidate exists.
     */
    @Query(
            """
            select ur from UnlockRequest ur
            where ur.device.id = :deviceId
              and ur.commandType = :commandType
              and ur.status = com.dadscare.backend.unlock.UnlockRequestStatus.RESPONDED
              and ur.succeeded = true
              and ur.relayedAt between :windowStart and :windowEnd
            order by ur.relayedAt desc
            """)
    List<UnlockRequest> findCorrelationCandidates(
            @Param("deviceId") Long deviceId,
            @Param("commandType") CommandType commandType,
            @Param("windowStart") Instant windowStart,
            @Param("windowEnd") Instant windowEnd);
}
