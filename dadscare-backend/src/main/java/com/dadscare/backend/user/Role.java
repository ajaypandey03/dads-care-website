package com.dadscare.backend.user;

/** Roles a user can hold, org-wide or scoped to a specific site via {@link UserSiteAccess}. */
public enum Role {
    ORG_ADMIN,
    SITE_MANAGER,
    VIEWER,
    OPERATOR
}
