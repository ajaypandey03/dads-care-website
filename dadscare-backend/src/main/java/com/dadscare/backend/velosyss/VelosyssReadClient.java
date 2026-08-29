package com.dadscare.backend.velosyss;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls Velosyss's read-only endpoints (§5 of the Integration Guide) — used by
 * {@code VelosyssPollingService} as the reconciliation safety net: positions for
 * device/shutter live state, events as a missed-webhook backstop. Not requester-facing;
 * nothing in the app calls this synchronously on a user action (that's
 * {@link VelosyssCommandClient}).
 */
@Slf4j
@Component
public class VelosyssReadClient {

    private final RestClient restClient;
    private final boolean configured;

    public VelosyssReadClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.velosyss.api-base-url:}") String baseUrl,
            @Value("${app.velosyss.api-key:}") String apiKey) {
        this.configured = !baseUrl.isBlank() && !apiKey.isBlank();
        this.restClient = this.configured
                ? restClientBuilder.baseUrl(baseUrl).defaultHeader("X-API-Key", apiKey).build()
                : null;
    }

    public boolean isConfigured() {
        return configured;
    }

    /** {@code GET /locks/positions} — all of the account's locks' live state in one call. */
    public List<PositionDto> getPositions() {
        if (!configured) {
            return List.of();
        }
        try {
            PositionDto[] positions = restClient.get().uri("/locks/positions").retrieve().body(PositionDto[].class);
            return positions == null ? List.of() : List.of(positions);
        } catch (RestClientException e) {
            log.warn("Velosyss positions poll failed: {}", e.getMessage());
            return List.of();
        }
    }

    /** {@code GET /locks/events?since=&limit=} — oldest-first, used to replay anything a webhook push might have missed. */
    public List<VelosyssWebhookEvent> getEvents(Instant since, int limit) {
        if (!configured) {
            return List.of();
        }
        try {
            VelosyssWebhookEvent[] events = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/locks/events")
                            .queryParam("since", since)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(VelosyssWebhookEvent[].class);
            return events == null ? List.of() : List.of(events);
        } catch (RestClientException e) {
            log.warn("Velosyss events poll failed: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * As reported by {@code GET /locks/positions} — see §5 example. {@code lastDeviceTime}
     * is deliberately {@link LocalDateTime}, not {@link Instant}: Velosyss's timestamps in
     * this API have no timezone suffix (e.g. {@code "2026-08-22T09:14:02"}), and the
     * backend treats them as UTC (matching {@code serverTimezone=UTC} on the datasource)
     * — see {@link #toInstantUtc}.
     */
    public record PositionDto(
            Long deviceId,
            String terminalId,
            Boolean isOnline,
            String status,
            Double latitude,
            Double longitude,
            Boolean sealed,
            Boolean shackleClosed,
            Boolean gpsValid,
            Integer batteryVoltageMv,
            LocalDateTime lastDeviceTime) {}

    /** Velosyss's timestamps (this API and the command API) carry no timezone suffix — treated as UTC. */
    public static Instant toInstantUtc(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(java.time.ZoneOffset.UTC);
    }
}
