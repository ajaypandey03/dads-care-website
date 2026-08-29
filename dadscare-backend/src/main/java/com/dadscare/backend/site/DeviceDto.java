package com.dadscare.backend.site;

import java.time.Instant;

public record DeviceDto(
        Long id,
        String velosyssDeviceRef,
        String velosyssTerminalId,
        Device.DeviceType type,
        String status,
        boolean online,
        Instant lastSeenAt,
        Integer lastBatteryPct,
        Double lastLatitude,
        Double lastLongitude,
        Boolean lastSealed,
        Boolean lastShackleClosed) {

    public static DeviceDto from(Device entity) {
        return new DeviceDto(
                entity.getId(),
                entity.getVelosyssDeviceRef(),
                entity.getVelosyssTerminalId(),
                entity.getType(),
                entity.getStatus(),
                entity.isOnline(),
                entity.getLastSeenAt(),
                entity.getLastBatteryPct(),
                entity.getLastLatitude(),
                entity.getLastLongitude(),
                entity.getLastSealed(),
                entity.getLastShackleClosed());
    }
}
