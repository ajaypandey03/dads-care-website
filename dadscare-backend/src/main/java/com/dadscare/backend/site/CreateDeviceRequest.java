package com.dadscare.backend.site;

import jakarta.validation.constraints.NotBlank;

/**
 * Registers a Velosyss Digital Lock device to the caller's org (e.g. once it's physically
 * installed) — {@code velosyssDeviceRef} is Velosyss's numeric lock id (used in every
 * REST command/read path — see VelosyssCommandClient/VelosyssReadClient) and
 * {@code velosyssTerminalId} is the separate identifier Velosyss stamps on every webhook
 * event (see WebhookService). Both must match Velosyss's records exactly — use the
 * {@code GET /locks} listing (surfaced via the admin "available locks" lookup, if wired
 * up) or ask Velosyss support if unsure. {@code velosyssTerminalId} is optional at
 * registration time (a device can be linked before its terminal id is confirmed) but
 * webhook events for it won't correlate until it's set. {@code shutterUnitId} is also
 * optional — a device can be registered before it's assigned to a shutter.
 */
public record CreateDeviceRequest(
        @NotBlank String velosyssDeviceRef, String velosyssTerminalId, Long shutterUnitId) {}
