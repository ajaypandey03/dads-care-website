package com.dadscare.backend.site;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.tenant.Organization;
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
 * A Velosyss Digital Lock device. Velosyss identifies a device two different ways and
 * this entity carries both, per the Integration Guide:
 * <ul>
 *   <li>{@code velosyssDeviceRef} — the numeric lock id used in REST paths, e.g.
 *       {@code POST /locks/{id}/commands} (see VelosyssCommandClient/VelosyssReadClient).
 *   <li>{@code velosyssTerminalId} — the stable {@code terminalId} every webhook event
 *       carries (see WebhookService) — this is the join key for inbound events, and is
 *       <em>not</em> the same value as the REST id.
 * </ul>
 * Devices are attributed to an {@link Organization} directly (not only via their
 * {@link ShutterUnit}) so a newly-provisioned device can receive telemetry before it's
 * been assigned to a shutter in the app.
 */
@Getter
@Setter
@Entity
@Table(name = "devices")
public class Device extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shutter_unit_id")
    private ShutterUnit shutterUnit;

    /** Velosyss's numeric lock id — used in every REST command/read call path. */
    @Column(name = "velosyss_device_ref", nullable = false, unique = true)
    private String velosyssDeviceRef;

    /** Velosyss's {@code terminalId} — the join key for every inbound webhook event. */
    @Column(name = "velosyss_terminal_id", unique = true)
    private String velosyssTerminalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeviceType type = DeviceType.DIGITAL_LOCK;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "is_online")
    private boolean online;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    /** Approximate battery percent, derived from {@link #lastBatteryMv} — see VelosyssPollingService. */
    @Column(name = "last_battery_pct")
    private Integer lastBatteryPct;

    /** Raw battery voltage (mV) from the last {@code GET /locks/positions} poll. */
    @Column(name = "last_battery_mv")
    private Integer lastBatteryMv;

    /** Cached from the last positions poll — informational only (dashboard live view), never authoritative. */
    @Column(name = "last_latitude")
    private Double lastLatitude;

    @Column(name = "last_longitude")
    private Double lastLongitude;

    @Column(name = "last_sealed")
    private Boolean lastSealed;

    @Column(name = "last_shackle_closed")
    private Boolean lastShackleClosed;

    public enum DeviceType {
        DIGITAL_LOCK
    }
}
