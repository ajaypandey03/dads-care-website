package com.dadscare.backend.site;

import com.dadscare.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * One physical shutter at a {@link Site}. Deliberately decoupled from {@link Device} —
 * a shutter's identity shouldn't change if the lock hardware on it is swapped, and a
 * future reed-switch sensor or camera (Phase 2/3 of the false-positive fix) attaches
 * here too, not to the lock device.
 */
@Getter
@Setter
@Entity
@Table(name = "shutter_units")
public class ShutterUnit extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String status = "ACTIVE";
}
