package com.dadscare.backend.telemetry;

import com.dadscare.backend.site.Device;
import com.dadscare.backend.tenant.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * A lock/telemetry event exactly as received from Velosyss's outbound push — immutable,
 * never edited after insert. This is Dad's Care's own durable record independent of
 * Velosyss's system of record, and the input the Rules & Alerts Engine (Phase 2) reads
 * from. Deliberately does NOT extend {@link com.dadscare.backend.common.BaseEntity}:
 * {@code receivedAt} (below) is the meaningful "created" timestamp here, and there is no
 * "updated" timestamp for an immutable row.
 */
@Getter
@Setter
@Entity
@Table(name = "raw_events")
public class RawEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /** Velosyss's own event id — the de-dup key for at-least-once delivery. */
    @Column(name = "velosyss_event_id", nullable = false, unique = true)
    private String velosyssEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    /** Raw lock state string as sent by Velosyss, kept verbatim alongside the normalized {@link #eventType}. */
    @Column(name = "lock_status")
    private String lockStatus;

    private Double latitude;

    private Double longitude;

    private Double speed;

    @Column(name = "battery_pct")
    private Integer batteryPct;

    /** Accelerometer/motion reading, if the device reported one on this event. */
    @Column(name = "motion_magnitude")
    private Double motionMagnitude;

    @Column(name = "tamper_flag")
    private Boolean tamperFlag;

    /** Which physical sensor produced this record — anticipates Phase 2 reed-switch sensors. */
    @Column(name = "source_sensor", length = 50)
    private String sourceSensor;

    /** When the event actually happened at the device, per Velosyss's payload. */
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    /** When this row was written — used for the webhook's own "since last run" style queries if ever needed. */
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    public enum EventType {
        LOCK_OPEN,
        LOCK_CLOSE,
        TAMPER,
        MOTION,
        HEARTBEAT
    }
}
