package com.dadscare.backend.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Self-service password change — requires knowing the current password, no email/reset-link flow yet. */
public record ChangePasswordRequest(
        @NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 100) String newPassword) {}
