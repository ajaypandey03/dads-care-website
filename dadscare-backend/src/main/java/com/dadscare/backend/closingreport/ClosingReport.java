package com.dadscare.backend.closingreport;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.site.Site;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * The day-level header of one godown's Closing Report — one row per (site, reportDate),
 * matching the customer's existing WhatsApp template (pasted into the implementation
 * chat): "Date", "# of Labourers", completed/standing truck counts, plus a per-product
 * breakdown carried by {@link ClosingReportLine}. This entity is storage only for now —
 * the WhatsApp notification format built on top of it is a deliberately separate,
 * not-yet-decided follow-up (see {@link ClosingReportService}).
 */
@Getter
@Setter
@Entity
@Table(name = "closing_reports")
public class ClosingReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "labour_count")
    private Integer labourCount;

    /** Trucks fully loaded/unloaded and dispatched during this report's period. */
    @Column(name = "completed_trucks")
    private Integer completedTrucks;

    /** Trucks still waiting (queued, not yet serviced) as of this report. */
    @Column(name = "standing_trucks")
    private Integer standingTrucks;

    @Column(length = 500)
    private String remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedBy;
}
