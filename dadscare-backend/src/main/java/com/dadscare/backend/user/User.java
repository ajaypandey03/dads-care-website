package com.dadscare.backend.user;

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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * The user's default/org-wide role. Site-specific overrides live in
     * {@link UserSiteAccess}; a user with no site-scoped rows at all is treated as
     * having this role across every site in their org.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.VIEWER;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled = false;

    @Column(nullable = false)
    private String status = "ACTIVE";
}
