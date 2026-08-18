package com.dadscare.backend.masterdata;

import jakarta.validation.constraints.NotBlank;

public record ProductMasterDto(Long id, @NotBlank String name, @NotBlank String unit, boolean active) {

    public static ProductMasterDto from(ProductMaster entity) {
        return new ProductMasterDto(entity.getId(), entity.getName(), entity.getUnit(), entity.isActive());
    }
}
