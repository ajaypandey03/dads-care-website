package com.dadscare.backend.user;

import jakarta.validation.constraints.NotNull;

/**
 * One entry in a {@code PUT /api/v1/users/{id}/site-access} request, which replaces the
 * user's <em>entire</em> set of {@link UserSiteAccess} overrides with the given list —
 * simplest semantics for an admin-facing "per-site role table" screen (send the whole
 * table back, not incremental add/remove calls). An empty list clears all overrides,
 * returning the user to their plain org-wide {@link User#getRole()} for every site.
 */
public record ReplaceSiteAccessRequest(@NotNull Long siteId, @NotNull Role role) {}
