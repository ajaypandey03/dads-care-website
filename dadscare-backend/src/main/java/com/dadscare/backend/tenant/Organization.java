package com.dadscare.backend.tenant;

import com.dadscare.backend.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A Dad's Care tenant — one of Dad's Care's own customers. Root of the multi-tenant
 * hierarchy (Organization -> Site -> ShutterUnit -> Device).
 */
@Getter
@Setter
@Entity
@Table(name = "organizations")
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    /** URL/slug-safe unique identifier, e.g. "acme-logistics". */
    @Column(nullable = false, unique = true)
    private String slug;

    /**
     * Short prefix used to format this org's sequential WhatsApp reference codes,
     * e.g. "DC" -> "DC-000482". See {@link com.dadscare.backend.sequence.SequenceCounterService}.
     */
    @Column(name = "code_prefix", nullable = false, length = 10)
    private String codePrefix;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * WhatsApp alert template values this org's data model has no field for — see
     * {@code RmlConnectWhatsAppSender}'s javadoc. Editable by Dad's Care platform admins
     * via the organization management screen.
     */
    @Column(name = "whatsapp_salutation", nullable = false, length = 100)
    private String whatsappSalutation = "Customer";

    @Column(name = "whatsapp_cfa_name", nullable = false)
    private String whatsappCfaName = "-";
}
