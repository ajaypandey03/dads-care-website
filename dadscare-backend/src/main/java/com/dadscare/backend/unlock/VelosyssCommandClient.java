package com.dadscare.backend.unlock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Calls Velosyss's Device Command API — {@code POST /api/v1/devices/{deviceRef}/commands}
 * — as a normal authenticated Enterprise API client (Bearer token), per the Integration
 * Contract page in Confluence. This is a real HTTP client, but note: as of Phase 2,
 * Velosyss's own {@code DeviceCommandAction} vocabulary does not yet include
 * {@code LOCK}/{@code UNLOCK} (see "Velosyss-side prerequisites" on the Implementation
 * Tracker) — every real call this client makes will fail until that ships. Failure is
 * handled gracefully here (returned as a {@link CommandResult}, never thrown to the
 * caller) specifically because that's the expected state today, not an edge case.
 */
@Slf4j
@Component
public class VelosyssCommandClient {

    private final RestClient restClient;
    private final boolean configured;

    public VelosyssCommandClient(
            @Value("${app.velosyss.api-base-url:}") String baseUrl,
            @Value("${app.velosyss.api-key:}") String apiKey) {
        this.configured = !baseUrl.isBlank() && !apiKey.isBlank();
        this.restClient = this.configured
                ? RestClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader("X-API-Key", apiKey)
                        .build()
                : null;
    }

    public CommandResult issueCommand(String velosyssDeviceRef, CommandType commandType, String requestId) {
        if (!configured) {
            log.warn(
                    "Velosyss API not configured (app.velosyss.api-base-url / api-key) — "
                            + "cannot relay {} command for device {}",
                    commandType,
                    velosyssDeviceRef);
            return CommandResult.failure("velosyss_api_not_configured");
        }

        try {
            var body = new CommandRequestBody(commandType.name(), requestId);
            restClient
                    .post()
                    .uri("/api/v1/devices/{deviceRef}/commands", velosyssDeviceRef)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return CommandResult.success();
        } catch (RestClientException e) {
            log.warn(
                    "Velosyss rejected {} command for device {} (requestId={}): {}",
                    commandType,
                    velosyssDeviceRef,
                    requestId,
                    e.getMessage());
            return CommandResult.failure(e.getMessage());
        }
    }

    private record CommandRequestBody(String command, String requestId) {
    }

    public record CommandResult(boolean accepted, String failureReason) {
        static CommandResult success() {
            return new CommandResult(true, null);
        }

        static CommandResult failure(String reason) {
            return new CommandResult(false, reason);
        }
    }
}
