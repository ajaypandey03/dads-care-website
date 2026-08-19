package com.dadscare.backend.user;

import jakarta.validation.constraints.NotNull;

/** Org-admin request to change a teammate's role or activate/deactivate them. */
public record UpdateUserRequest(@NotNull Role role, @NotNull String status) {}
