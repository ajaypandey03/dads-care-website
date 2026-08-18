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
 * legitimate command it issued, so a {@code LOCK_OPEN}/{@code LOCK_CLOSE} telemetry event
 * with no matching row here is exactly the "unexplained access" case Dad's Care cares
 * about. See the Lock vs Shutter page in Confluence.
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

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false, length = 20)
    private CommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnlockRequestStatus status = UnlockRequestStatus.PENDING;

    /** Idempotency key sent to Velosyss's Device Command API — see VelosyssCommandClient. */
    @Column(name = "velosyss_request_id", nullable = false, unique = true)
    private String velosyssRequestId;

    @Column(name = "relayed_at")
    private Instant relayedAt;

    @Column(name = "failure_reason")
    private String failureReason;
}
