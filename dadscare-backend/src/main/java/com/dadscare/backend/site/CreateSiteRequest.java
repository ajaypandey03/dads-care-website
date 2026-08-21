package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

/** Registers a new godown for the caller's org. Starts ACTIVE — see {@link UpdateSiteRequest} to change that. */
public record CreateSiteRequest(@NotBlank String name, @NotBlank String godownCode, String address) {}
