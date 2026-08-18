package com.dadscare.backend.telemetry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * One event in the payload Velosyss's Outbound Integration Service pushes, per the
 * Integration Contract in Confluence. Field names match the agreed contract exactly —
 * do not rename without updating the contract on both sides.
 */
public record LockEventPayload(
        @NotBlank String eventId,
        @NotBlank String deviceRef,
        String lockStatus,
        Double lat,
        Double lng,
        Double speed,
        Integer battery,
        @NotNull Instant eventTimestamp,
        String tenantRef,
        @NotNull EventType eventType,
        Double motionMagnitude,
        Boolean tamperFlag,
        String sourceSensor) {

    public enum EventType {
        LOCK_OPEN,
        LOCK_CLOSE,
        TAMPER,
        MOTION,
        HEARTBEAT
    }
}
