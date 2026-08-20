package com.dadscare.backend.platform;

import com.dadscare.backend.user.UserAdminDto;

/**
 * {@code temporaryPassword} is returned exactly once, here, and never stored or logged in
 * plaintext elsewhere — same convention as {@code CreateUserResponse} for a normal team
 * invite. Relay it to the new org's admin out-of-band.
 */
public record CreateOrganizationResponse(
        OrganizationDto organization, UserAdminDto adminUser, String temporaryPassword) {}
