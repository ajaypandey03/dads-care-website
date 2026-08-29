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
 * Every Velosyss webhook event exactly as received — immutable, never edited after
 * insert — normalized into one of four internal types (see {@link EventType}):
 * <ul>
 *   <li>{@code SEAL_STATE} (§4.2) is translated to {@code LOCK_OPEN}/{@code LOCK_CLOSE}
 *       here — {@code sealed=false} is an open, {@code sealed=true} a close — the
 *       vocabulary the rest of the platform (SiteService's live shutter state,
 *       RulesEngineService's correlation) already speaks. The raw {@link #sealed}/
 *       {@link #shackleClosed} booleans are kept alongside for audit fidelity.
 *   <li>{@code ALARM} is kept as-is — always alert-worthy, never scored (see
 *       RulesEngineService#evaluateAlarm).
 *   <li>{@code COMMAND_RESULT} is kept as-is for the audit trail, but never triggers an
 *       Alert on its own — WebhookService instead applies it to the originating
 *       {@link com.dadscare.backend.unlock.UnlockRequest} (see UnlockRequestService).
 * </ul>
 * This is Dad's Care's own durable record independent of Velosyss's system of record.
 * Deliberately does NOT extend {@link com.dadscare.backend.common.BaseEntity}:
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

    /** Velosyss's own event id — the de-dup key for at-least-once delivery (§4.4), shared across all event types. */
    @Column(name = "velosyss_event_id", nullable = false, unique = true)
    private String velosyssEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    // ---- SEAL_STATE (§4.2) ----

    /** Electronic seal state as Velosyss reported it — {@code eventType} is already the normalized OPEN/CLOSE direction. */
    private Boolean sealed;

    /** Physical shackle position — related to but distinct from {@link #sealed}, per §4.2. */
    @Column(name = "shackle_closed")
    private Boolean shackleClosed;

    // ---- ALARM (§4.2) ----

    /** One of the {@code alarm} enum values in §4.2 (e.g. {@code SHACKLE_CUT}). */
    @Column(name = "alarm_code")
    private String alarmCode;

    @Column(name = "alarm_description")
    private String alarmDescription;

    /**
     * True if an ALARM landed on this device shortly before/after this event — computed
     * by WebhookService at ingest time, since Velosyss no longer sends a raw tamper/motion
     * flag on the SEAL_STATE event itself. Feeds RulesEngineService's secondary heuristic
     * score in place of the sensor-level signal the original design assumed.
     */
    @Column(name = "tamper_flag")
    private Boolean tamperFlag;

    // ---- COMMAND_RESULT (§4.2) ----

    @Column(name = "command_request_id")
    private String commandRequestId;

    @Column(name = "command_action")
    private String commandAction;

    @Column(name = "command_status")
    private String commandStatus;

    @Column(name = "command_succeeded")
    private Boolean commandSucceeded;

    @Column(name = "command_message")
    private String commandMessage;

    /**
     * When this row was written — used as the ordering/correlation-window key throughout
     * (SiteService's live shutter state, RulesEngineService's correlation and
     * quick-reclose checks). Velosyss's webhook payloads carry no event-level timestamp
     * of their own (unlike the old assumed contract), so this is simply "when we received
     * it" — kept as its own column (rather than reusing {@link #receivedAt}) to preserve
     * the existing {@code RawEventRepository} query method names untouched.
     */
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp = Instant.now();

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    public enum EventType {
        LOCK_OPEN,
        LOCK_CLOSE,
        ALARM,
        COMMAND_RESULT
    }
}
