package com.dadscare.backend.platform;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edits an existing organization. {@code slug} is deliberately not editable here — it's
 * the stable identifier other systems could reference, so renaming it is a bigger,
 * separate decision than the day-to-day support edits this endpoint is for.
 */
public record UpdateOrganizationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank
                @Pattern(regexp = "^[A-Z0-9]+$", message = "codePrefix must be uppercase letters/digits, e.g. \"AC\"")
                @Size(max = 10)
                String codePrefix,
        @NotNull Boolean active) {}
