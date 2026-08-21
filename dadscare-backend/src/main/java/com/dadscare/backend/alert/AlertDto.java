package com.dadscare.backend.alert;

import java.time.Instant;

public record AlertDto(
        Long id,
        Long deviceId,
        String deviceRef,
        Long siteId,
        String siteName,
        EventDirection direction,
        AlertClassification classification,
        Integer confidenceScore,
        String sequenceCode,
        Instant createdAt,
        // null = no feedback submitted yet; true/false = the most recent "Was this correct?"
        // answer — lets the dashboard stop re-asking once an alert has been answered.
        Boolean feedbackCorrect) {

    public static AlertDto from(Alert entity, Boolean feedbackCorrect) {
        var device = entity.getDevice();
        var shutterUnit = device.getShutterUnit();
        return new AlertDto(
                entity.getId(),
                device.getId(),
                device.getVelosyssDeviceRef(),
                shutterUnit == null ? null : shutterUnit.getSite().getId(),
                shutterUnit == null ? null : shutterUnit.getSite().getName(),
                entity.getDirection(),
                entity.getClassification(),
                entity.getConfidenceScore(),
                entity.getSequenceCode(),
                entity.getCreatedAt(),
                feedbackCorrect);
    }
}
