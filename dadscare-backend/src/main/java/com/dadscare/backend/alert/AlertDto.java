package com.dadscare.backend.alert;

import java.time.Instant;

public record AlertDto(
        Long id,
        Long deviceId,
        EventDirection direction,
        AlertClassification classification,
        Integer confidenceScore,
        String sequenceCode,
        Instant createdAt) {

    public static AlertDto from(Alert entity) {
        return new AlertDto(
                entity.getId(),
                entity.getDevice().getId(),
                entity.getDirection(),
                entity.getClassification(),
                entity.getConfidenceScore(),
                entity.getSequenceCode(),
                entity.getCreatedAt());
    }
}
