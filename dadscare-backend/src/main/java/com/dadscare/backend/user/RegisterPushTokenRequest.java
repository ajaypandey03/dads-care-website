package com.dadscare.backend.user;

import jakarta.validation.constraints.NotBlank;

/** {@code token} is an Expo push token (e.g. "ExponentPushToken[xxxxxxxx]") from dadscare-mobile. */
public record RegisterPushTokenRequest(@NotBlank String token) {
}
