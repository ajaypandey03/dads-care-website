package com.dadscare.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Org-admin request to invite a new teammate into the tenant. {@code password} is optional —
 * when omitted, a random temporary password is generated (see {@code CreateUserResponse}); when
 * supplied, the admin sets it directly and no temporary password is generated or returned.
 * Either way there's no email/SMTP integration yet, so the admin must relay it out-of-band.
 */
public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        @NotNull Role role,
        @Size(min = 8, max = 100, message = "password must be at least 8 characters") String password) {}
