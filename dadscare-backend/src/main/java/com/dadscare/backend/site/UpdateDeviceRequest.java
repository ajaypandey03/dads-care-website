package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

/** {@code shutterUnitId} null unassigns the device from any shutter. */
public record UpdateDeviceRequest(
        @NotBlank String velosyssDeviceRef, String velosyssTerminalId, Long shutterUnitId, @NotBlank String status) {}
