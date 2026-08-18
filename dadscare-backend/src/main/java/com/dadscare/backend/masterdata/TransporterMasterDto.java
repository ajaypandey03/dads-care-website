package com.dadscare.backend.masterdata;

import jakarta.validation.constraints.NotBlank;

public record TransporterMasterDto(Long id, @NotBlank String name, String code, boolean active) {

    public static TransporterMasterDto from(TransporterMaster entity) {
        return new TransporterMasterDto(entity.getId(), entity.getName(), entity.getCode(), entity.isActive());
    }
}
