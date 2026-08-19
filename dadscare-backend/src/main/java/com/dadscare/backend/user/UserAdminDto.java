package com.dadscare.backend.user;

/** Org-admin view of a user — like {@link UserDto} but includes status, which /me callers don't need. */
public record UserAdminDto(Long id, String name, String email, String phone, Role role, String status) {

    public static UserAdminDto from(User entity) {
        return new UserAdminDto(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getRole(),
                entity.getStatus());
    }
}
