package com.dadscare.backend.site;

public record SiteDto(Long id, String name, String godownCode, String address, String status) {

    public static SiteDto from(Site entity) {
        return new SiteDto(entity.getId(), entity.getName(), entity.getGodownCode(), entity.getAddress(), entity.getStatus());
    }
}
