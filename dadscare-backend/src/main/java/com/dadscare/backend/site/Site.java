package com.dadscare.backend.site;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.tenant.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** A godown/warehouse belonging to an {@link Organization}. */
@Getter
@Setter
@Entity
@Table(name = "sites")
public class Site extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    /** Godown ID as configured on the operator's device during install — see Godown Operational Workflow. */
    @Column(name = "godown_code", nullable = false)
    private String godownCode;

    private String address;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private String timezone = "Asia/Kolkata";

    @Column(nullable = false)
    private String status = "ACTIVE";
}
