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

/** Tenant-scoped product catalog entry, used to populate stock-line dropdowns in Opening/Closing forms. */
@Getter
@Setter
@Entity
@Table(name = "product_masters")
public class ProductMaster extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(nullable = false)
    private boolean active = true;
}
