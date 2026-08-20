package com.dadscare.backend.platform;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Onboards a new Dad's Care customer: creates its Organization and its first ORG_ADMIN user. */
public record CreateOrganizationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank
                @Pattern(
                        regexp = "^[a-z0-9]+(-[a-z0-9]+)*$",
                        message = "slug must be lowercase-kebab-case, e.g. \"acme-logistics\"")
                @Size(max = 255)
                String slug,
        @NotBlank
                @Pattern(regexp = "^[A-Z0-9]+$", message = "codePrefix must be uppercase letters/digits, e.g. \"AC\"")
                @Size(max = 10)
                String codePrefix,
        @NotBlank @Size(max = 255) String adminName,
        @NotBlank @Email String adminEmail,
        String adminPhone) {}
