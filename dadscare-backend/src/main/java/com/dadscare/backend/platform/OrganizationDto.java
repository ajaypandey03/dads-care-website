package com.dadscare.backend.platform;

import com.dadscare.backend.tenant.Organization;
import java.time.Instant;

public record OrganizationDto(
        Long id, String name, String slug, String codePrefix, boolean active, Instant createdAt) {

    public static OrganizationDto from(Organization entity) {
        return new OrganizationDto(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getCodePrefix(),
                entity.isActive(),
                entity.getCreatedAt());
    }
}
