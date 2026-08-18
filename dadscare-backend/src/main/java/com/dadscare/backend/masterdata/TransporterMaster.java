package com.dadscare.backend.masterdata;

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

/** Tenant-scoped transporter catalog entry, used to populate truck-entry dropdowns in Opening/Closing forms. */
@Getter
@Setter
@Entity
@Table(name = "transporter_masters")
public class TransporterMaster extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(length = 30)
    private String code;

    @Column(nullable = false)
    private boolean active = true;
}
