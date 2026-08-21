package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

public record UpdateSiteRequest(
        @NotBlank String name, @NotBlank String godownCode, String address, @NotBlank String status) {}
