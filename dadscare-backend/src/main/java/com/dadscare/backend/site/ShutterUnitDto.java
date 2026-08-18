package com.dadscare.backend.site;

public record ShutterUnitDto(Long id, Long siteId, String label, String status, DeviceDto device) {

    public static ShutterUnitDto from(ShutterUnit entity, Device device) {
        return new ShutterUnitDto(
                entity.getId(),
                entity.getSite().getId(),
                entity.getLabel(),
                entity.getStatus(),
                device == null ? null : DeviceDto.from(device));
    }
}
