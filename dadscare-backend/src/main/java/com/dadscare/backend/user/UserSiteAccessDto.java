package com.dadscare.backend.user;

/** One site-scoped role override for a user — see {@link UserSiteAccess}. */
public record UserSiteAccessDto(Long siteId, String siteName, Role role) {

    public static UserSiteAccessDto from(UserSiteAccess entity) {
        return new UserSiteAccessDto(entity.getSite().getId(), entity.getSite().getName(), entity.getRole());
    }
}
