package com.dadscare.backend.user;

public record UserDto(Long id, Long organizationId, String name, String email, String phone, Role role) {

    public static UserDto from(User entity) {
        return new UserDto(
                entity.getId(),
                entity.getOrganization().getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getRole());
    }
}
