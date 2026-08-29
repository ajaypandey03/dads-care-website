package com.dadscare.backend.velosyss;

import com.dadscare.backend.unlock.CommandType;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Calls Velosyss's real Device Command API — {@code POST /locks/{id}/commands}, relative
 * to {@code app.velosyss.api-base-url} (which already includes {@code /api/v1} — see
 * "Velosyss Lock Integration Guide" §6.2 in Confluence) — authenticated with an
 * {@code X-API-Key} header. Commands are asynchronous: this call only tells us Velosyss
 * *accepted* the request (and hands back the {@code requestId} it assigned); the actual
 * outcome always arrives later, either via the {@code COMMAND_RESULT} webhook (see
 * {@code WebhookService}) or by polling (§6.3) — never assume success just because this
 * call returned 2xx.
 */
@Slf4j
@Component
public class VelosyssCommandClient {

    private final RestClient restClient;
    private final boolean configured;

    public VelosyssCommandClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.velosyss.api-base-url:}") String baseUrl,
            @Value("${app.velosyss.api-key:}") String apiKey) {
        this.configured = !baseUrl.isBlank() && !apiKey.isBlank();
        // Built from the Spring Boot-autoconfigured RestClient.Builder (not a bare
        // RestClient.builder()) so it inherits the app's Jackson setup — in particular
        // jackson-datatype-jsr310, needed to deserialize CommandResponseBody's LocalDateTime fields.
        this.restClient = this.configured
                ? restClientBuilder.baseUrl(baseUrl).defaultHeader("X-API-Key", apiKey).build()
                : null;
    }

    /**
     * @param velosyssDeviceRef Velosyss's numeric lock id (Device#velosyssDeviceRef), used directly in the URL path
     * @param commandType Dad's Care's own vocabulary — mapped to Velosyss's {@code action} below
     * @param operator free-text audit-trail identifier (the requesting user's name), per §6.2
     */
    public CommandResult issueCommand(String velosyssDeviceRef, CommandType commandType, String operator) {
        if (!configured) {
            log.warn(
                    "Velosyss API not configured (app.velosyss.api-base-url / api-key) — "
                            + "cannot relay {} command for lock {}",
                    commandType,
                    velosyssDeviceRef);
            return CommandResult.failure("velosyss_api_not_configured");
        }

        try {
            var body = new CommandRequestBody(toVelosyssAction(commandType), operator);
            CommandResponseBody response = restClient
                    .post()
                    .uri("/locks/{id}/commands", velosyssDeviceRef)
                    .body(body)
                    .retrieve()
                    .body(CommandResponseBody.class);
            return CommandResult.success(response);
        } catch (RestClientResponseException e) {
            // 401/403/404/422 etc — Velosyss rejected the request outright (see §7). The
            // response body is Velosyss's own JSON error, useful verbatim in failureReason.
            log.warn(
                    "Velosyss rejected {} command for lock {} (operator={}): HTTP {} — {}",
                    commandType,
                    velosyssDeviceRef,
                    operator,
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());
            return CommandResult.failure("velosyss_http_" + e.getStatusCode().value() + ": " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.warn(
                    "Velosyss command call failed for lock {} (operator={}): {}",
                    velosyssDeviceRef,
                    operator,
                    e.getMessage());
            return CommandResult.failure(e.getMessage());
        }
    }

    /** §6.1 — Dad's Care only ever issues the two primary commands; the others (FORCE_RESTART etc.) aren't user-facing yet. */
    private String toVelosyssAction(CommandType commandType) {
        return switch (commandType) {
            case LOCK -> "SEAL";
            case UNLOCK -> "UNSEAL";
        };
    }

    private record CommandRequestBody(String action, String operator) {}

    /**
     * Shape of the response body documented in §6.2. Timestamps are {@link LocalDateTime}
     * — Velosyss sends no timezone suffix; see {@link VelosyssReadClient#toInstantUtc}.
     */
    public record CommandResponseBody(
            String requestId,
            Long deviceId,
            String action,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime dispatchedAt,
            LocalDateTime respondedAt,
            LocalDateTime expiredAt,
            String message,
            Boolean responseSucceeded) {}

    public record CommandResult(boolean accepted, CommandResponseBody response, String failureReason) {
        static CommandResult success(CommandResponseBody response) {
            return new CommandResult(true, response, null);
        }

        static CommandResult failure(String reason) {
            return new CommandResult(false, null, reason);
        }
    }
}
