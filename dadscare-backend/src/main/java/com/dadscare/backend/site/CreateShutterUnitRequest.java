package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

public record CreateShutterUnitRequest(@NotBlank String label) {}
