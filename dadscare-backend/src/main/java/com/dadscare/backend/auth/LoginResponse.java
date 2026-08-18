package com.dadscare.backend.auth;

public record LoginResponse(String accessToken, String tokenType, Long userId, Long organizationId, String role) {

    public static LoginResponse bearer(String accessToken, Long userId, Long organizationId, String role) {
        return new LoginResponse(accessToken, "Bearer", userId, organizationId, role);
    }
}
