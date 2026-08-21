package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

public record UpdateShutterUnitRequest(@NotBlank String label, @NotBlank String status) {}
