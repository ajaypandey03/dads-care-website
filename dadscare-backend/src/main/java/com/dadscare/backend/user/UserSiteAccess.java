package com.dadscare.backend.user;

import com.dadscare.backend.common.BaseEntity;
import com.dadscare.backend.site.Site;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * Grants a {@link User} a {@link Role} scoped to one {@link Site}, overriding their
 * org-wide default role for that site only. Absence of a row for (user, site) falls
 * back to {@link User#getRole()}.
 */
@Getter
@Setter
@Entity
@Table(
        name = "user_site_access",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "site_id"}))
public class UserSiteAccess extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;
}
