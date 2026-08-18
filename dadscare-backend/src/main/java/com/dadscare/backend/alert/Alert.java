package com.dadscare.backend.alert;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.site.Device;
import com.dadscare.backend.telemetry.RawEvent;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.unlock.UnlockRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One classified lock-state event, the output of {@link RulesEngineService}. */
@Getter
@Setter
@Entity
@Table(name = "alerts")
public class Alert extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /** The telemetry event that triggered this alert — one Alert per LOCK_OPEN/LOCK_CLOSE RawEvent. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "raw_event_id", nullable = false, unique = true)
    private RawEvent rawEvent;

    /** Set only for {@link AlertClassification#CONFIRMED} — the request this event fulfilled. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unlock_request_id")
    private UnlockRequest unlockRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EventDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertClassification classification;

    /** Secondary-heuristic confidence score (0-100). Null for CONFIRMED, which is deterministic, not scored. */
    @Column(name = "confidence_score")
    private Integer confidenceScore;

    /**
     * Customer-facing sequential reference code (e.g. "DC-000482"), assigned only for
     * classifications that actually get notified (everything except SUPPRESSED). See
     * {@link com.dadscare.backend.sequence.SequenceCounterService}.
     */
    @Column(name = "sequence_code")
    private String sequenceCode;
}
