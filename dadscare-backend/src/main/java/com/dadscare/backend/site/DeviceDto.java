package com.dadscare.backend.site;

import java.time.Instant;

public record DeviceDto(
        Long id, String velosyssDeviceRef, Device.DeviceType type, String status, boolean online,
        Instant lastSeenAt, Integer lastBatteryPct) {

    public static DeviceDto from(Device entity) {
        return new DeviceDto(
                entity.getId(),
                entity.getVelosyssDeviceRef(),
                entity.getType(),
                entity.getStatus(),
                entity.isOnline(),
                entity.getLastSeenAt(),
                entity.getLastBatteryPct());
    }
}
