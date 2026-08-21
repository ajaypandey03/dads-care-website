package com.dadscare.backend.site;

import java.time.Instant;

public record ShutterUnitDto(
        Long id,
        Long siteId,
        String label,
        String status,
        DeviceDto device,
        ShutterState currentState,
        Instant lastOpenedAt,
        Instant lastClosedAt) {

    public static ShutterUnitDto from(
            ShutterUnit entity, Device device, ShutterState currentState, Instant lastOpenedAt, Instant lastClosedAt) {
        return new ShutterUnitDto(
                entity.getId(),
                entity.getSite().getId(),
                entity.getLabel(),
                entity.getStatus(),
                device == null ? null : DeviceDto.from(device),
                currentState,
                lastOpenedAt,
                lastClosedAt);
    }
}
