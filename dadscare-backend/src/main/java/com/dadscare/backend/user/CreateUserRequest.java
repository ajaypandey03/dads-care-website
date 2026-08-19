package com.dadscare.backend.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Org-admin request to invite a new teammate into the tenant. */
public record CreateUserRequest(
        @NotBlank String name, @NotBlank @Email String email, String phone, @NotNull Role role) {}
