package com.dadscare.backend.unlock;

import jakarta.validation.constraints.NotNull;

public record CreateUnlockRequestRequest(@NotNull CommandType commandType) {
}
