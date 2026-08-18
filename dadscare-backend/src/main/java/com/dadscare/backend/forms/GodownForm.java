package com.dadscare.backend.forms;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.tenant.Organization;
import com.dadscare.backend.unlock.UnlockRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * The operational data an operator submits alongside an {@link UnlockRequest} — the
 * Opening/Closing form from Dad's Care's original spec (see Godown Operational Workflow
 * in Confluence). One form per unlock request; {@link StockLine}s and {@link TruckEntry}s
 * hang off it. Custom fields (up to 10, per the original spec) are stored as JSON — they're
 * genuinely freeform key/value pairs, not worth a generic EAV table for the query patterns
 * this needs.
 */
@Getter
@Setter
@Entity
@Table(name = "godown_forms")
public class GodownForm extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unlock_request_id", nullable = false, unique = true)
    private UnlockRequest unlockRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "labor_count")
    private Integer laborCount;

    @Column(length = 200)
    private String remarks;

    /** JSON array of {@code {"heading": "...", "value": "..."}} — max 10, enforced in the DTO/service, not the DB. */
    @Column(name = "custom_fields_json", columnDefinition = "TEXT")
    private String customFieldsJson;
}
