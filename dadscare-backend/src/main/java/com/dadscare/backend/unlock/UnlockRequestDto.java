package com.dadscare.backend.unlock;

import java.time.Instant;

public record UnlockRequestDto(
        Long id,
        Long deviceId,
        CommandType commandType,
        UnlockRequestStatus status,
        Boolean succeeded,
        String message,
        Instant createdAt) {

    public static UnlockRequestDto from(UnlockRequest entity) {
        return new UnlockRequestDto(
                entity.getId(),
                entity.getDevice().getId(),
                entity.getCommandType(),
                entity.getStatus(),
                entity.getSucceeded(),
                entity.getMessage() != null ? entity.getMessage() : entity.getFailureReason(),
                entity.getCreatedAt());
    }
}
