package com.dadscare.backend.velosyss;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One event as Velosyss actually delivers it — a single JSON object per {@code POST}, not
 * a batch — per §4.2 of the Integration Guide. Field names match the documented contract
 * exactly. Every event carries {@code eventId}/{@code eventType}/{@code integrationCode}/
 * {@code terminalId}; the rest are only populated for the matching {@code eventType}.
 * This same shape is reused for events replayed from {@code GET /locks/events} during
 * reconciliation polling (see VelosyssPollingService) — the wire format is identical.
 */
public record VelosyssWebhookEvent(
        @NotBlank String eventId,
        @NotNull EventType eventType,
        String integrationCode,
        @NotBlank String terminalId,

        // ALARM
        String alarm,
        String description,

        // SEAL_STATE
        Boolean sealed,
        Boolean shackleClosed,

        // COMMAND_RESULT
        String requestId,
        String action,
        String status,
        Boolean succeeded,
        String message) {

    public enum EventType {
        ALARM,
        SEAL_STATE,
        COMMAND_RESULT
    }
}
