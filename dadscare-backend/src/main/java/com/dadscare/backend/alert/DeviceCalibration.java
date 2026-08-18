package com.dadscare.backend.alert;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.site.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Per-device tunable weights for {@link RulesEngineService}'s secondary (motion/tamper)
 * heuristic, adjusted over time from {@link FeedbackEntry} review — see "Lock vs Shutter"
 * in Confluence. A device with no row here uses the class-level defaults in
 * {@link RulesEngineService}; this table only holds overrides.
 */
@Getter
@Setter
@Entity
@Table(name = "device_calibrations")
public class DeviceCalibration extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    private Device device;

    /** Points awarded when tamperFlag is true or motionMagnitude exceeds the threshold. Default 35. */
    @Column(name = "tamper_motion_weight", nullable = false)
    private int tamperMotionWeight = 35;

    /** Points awarded when the open was NOT immediately re-closed (a real "someone was in there" signal). Default 25. */
    @Column(name = "duration_weight", nullable = false)
    private int durationWeight = 25;

    /** Score at/above which an unmatched event escalates to UNEXPLAINED_HIGH. Default 70. */
    @Column(name = "escalate_threshold", nullable = false)
    private int escalateThreshold = 70;

    /** Score at/above which an unmatched event is at least UNEXPLAINED_VERIFY (else SUPPRESSED). Default 40. */
    @Column(name = "verify_threshold", nullable = false)
    private int verifyThreshold = 40;

    /** A LOCK_CLOSE within this many seconds of the LOCK_OPEN counts as an immediate reclose. Default 3. */
    @Column(name = "quick_reclose_window_seconds", nullable = false)
    private int quickRecloseWindowSeconds = 3;
}
