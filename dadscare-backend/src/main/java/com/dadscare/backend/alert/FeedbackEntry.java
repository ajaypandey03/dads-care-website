package com.dadscare.backend.alert;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A customer's "Was this correct?" response to an {@link Alert} — the raw material for
 * periodically tuning {@link DeviceCalibration} weights per device. See "Lock vs Shutter"
 * in Confluence.
 */
@Getter
@Setter
@Entity
@Table(name = "feedback_entries")
public class FeedbackEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedBy;

    @Column(name = "was_correct", nullable = false)
    private boolean wasCorrect;

    @Column(length = 500)
    private String comment;
}
