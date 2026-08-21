package com.dadscare.backend.platform;

import com.dadscare.backend.user.UserAdminDto;

/**
 * {@code temporaryPassword} is returned exactly once, here — same one-time-reveal
 * convention as {@link CreateOrganizationResponse}/{@code CreateUserResponse}. Lets a
 * platform admin get a locked-out customer admin back into their account.
 */
public record ResetPasswordResponse(UserAdminDto user, String temporaryPassword) {}
