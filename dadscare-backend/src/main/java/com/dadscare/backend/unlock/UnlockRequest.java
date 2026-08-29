package com.dadscare.backend.unlock;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * A record of {@code dadscare-backend} itself asking Velosyss to operate a lock — created
 * and relayed <em>before</em> the physical action happens, never reconstructed after the
 * fact. This is what makes {@link com.dadscare.backend.alert.RulesEngineService}'s
 * Authorized-Open Correlation close to deterministic: the backend already knows every
 * legitimate command it issued, so a {@code SEAL_STATE} transition with no matching
 * successfully-{@code RESPONDED} row here is exactly the "unexplained access" case Dad's
 * Care cares about. See the Lock vs Shutter page in Confluence.
 */
@Getter
@Setter
@Entity
@Table(name = "unlock_requests")
public class UnlockRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    /** Dad's Care's own vocabulary — see {@link VelosyssCommandClient} for the mapping to Velosyss's {@code SEAL}/{@code UNSEAL}. */
    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false, length = 20)
    private CommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnlockRequestStatus status = UnlockRequestStatus.PENDING;

    /**
     * Velosyss's own {@code requestId}, from the command response (§6.2) — Velosyss issues
     * this, we don't; null until the first successful API response, then the key every
     * {@code COMMAND_RESULT} webhook is correlated against (see UnlockRequestService).
     */
    @Column(name = "velosyss_request_id", unique = true)
    private String velosyssRequestId;

    /** Set the moment Velosyss's command API first accepts the request (any 2xx), regardless of final outcome. */
    @Column(name = "relayed_at")
    private Instant relayedAt;

    @Column(name = "dispatched_at")
    private Instant dispatchedAt;

    /** When the {@code COMMAND_RESULT} webhook reported a terminal RESPONDED/EXPIRED/DEVICE_OFFLINE outcome. */
    @Column(name = "responded_at")
    private Instant respondedAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    /** Only meaningful once {@link #status} is RESPONDED — the lock's own reported success/failure. */
    private Boolean succeeded;

    /** Human-readable detail from Velosyss — either the command response or the COMMAND_RESULT webhook. */
    private String message;

    @Column(name = "failure_reason")
    private String failureReason;
}
