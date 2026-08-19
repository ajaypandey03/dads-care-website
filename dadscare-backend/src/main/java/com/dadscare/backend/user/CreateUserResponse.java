package com.dadscare.backend.user;

/**
 * Response to a user-invite. {@code temporaryPassword} is returned exactly once, here, and never
 * stored or logged in plaintext anywhere else — the admin is expected to relay it to the new
 * teammate out-of-band. There's no forced-reset-on-first-login flow yet (see README limitations).
 */
public record CreateUserResponse(UserAdminDto user, String temporaryPassword) {}
