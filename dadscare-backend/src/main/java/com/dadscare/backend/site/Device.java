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
 * A Velosyss Digital Lock device, mapped 1:1 to Velosyss's {@code deviceRef}. Devices are
 * attributed to an {@link Organization} directly (not only via their {@link ShutterUnit})
 * so a newly-provisioned device can receive telemetry before it's been assigned to a
 * shutter in the app.
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

    /** Velosyss's own device identifier — the join key for every inbound webhook event. */
    @Column(name = "velosyss_device_ref", nullable = false, unique = true)
    private String velosyssDeviceRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeviceType type = DeviceType.DIGITAL_LOCK;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "is_online")
    private boolean online;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Column(name = "last_battery_pct")
    private Integer lastBatteryPct;

    public enum DeviceType {
        DIGITAL_LOCK
    }
}
