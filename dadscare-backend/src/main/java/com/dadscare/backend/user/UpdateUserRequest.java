package com.dadscare.backend.user;

import jakarta.validation.constraints.NotNull;

/** Org-admin request to change a teammate's role, phone number, or activate/deactivate them. {@code phone} is optional — omit/null to leave it unchanged. */
public record UpdateUserRequest(@NotNull Role role, @NotNull String status, String phone) {}
