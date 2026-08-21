package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

/**
 * Registers a Velosyss Digital Lock device to the caller's org (e.g. once it's physically
 * installed) — {@code velosyssDeviceRef} must match Velosyss's own identifier exactly, since
 * that's the join key every inbound webhook event is matched against. {@code shutterUnitId}
 * is optional — a device can be registered before it's assigned to a shutter.
 */
public record CreateDeviceRequest(@NotBlank String velosyssDeviceRef, Long shutterUnitId) {}
