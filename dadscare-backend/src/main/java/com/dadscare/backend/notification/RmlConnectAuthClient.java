package com.dadscare.backend.notification;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Logs into RMLConnect's WhatsApp Business API (Route Mobile) and caches the JWT — see
 * {@code https://developer.rmlconnect.net/route-mobile-project/docs/whatsapp-login}.
 * {@code POST /auth/v1/login/} with {@code {username, password}} returns a JWT
 * ({@code JWTAUTH}) documented as valid for one hour; this refreshes a few minutes early
 * rather than waiting for a 401 mid-send. Not thread-contended enough to need more than
 * {@code synchronized} — WhatsApp sends are low-volume (alert-triggered, not bulk).
 */
@Slf4j
@Component
public class RmlConnectAuthClient {

    /** Refresh this long before the documented 1-hour expiry, to absorb clock drift and slow calls. */
    private static final long REFRESH_MARGIN_SECONDS = 5 * 60;

    private final RestClient restClient;
    private final String username;
    private final String password;
    private final boolean configured;

    private String cachedToken;
    private Instant cachedTokenExpiresAt = Instant.EPOCH;

    public RmlConnectAuthClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.whatsapp.rmlconnect.base-url:https://apis.rmlconnect.net}") String baseUrl,
            @Value("${app.whatsapp.rmlconnect.username:}") String username,
            @Value("${app.whatsapp.rmlconnect.password:}") String password) {
        this.username = username;
        this.password = password;
        this.configured = !username.isBlank() && !password.isBlank();
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public boolean isConfigured() {
        return configured;
    }

    /** @return a currently-valid JWT, logging in (or re-logging in) first if needed; null if unconfigured or login failed. */
    public synchronized String token() {
        if (!configured) {
            return null;
        }
        if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiresAt)) {
            return cachedToken;
        }
        try {
            LoginResponse response = restClient
                    .post()
                    .uri("/auth/v1/login/")
                    .body(new LoginRequest(username, password))
                    .retrieve()
                    .body(LoginResponse.class);
            if (response == null || response.jwtauth() == null) {
                log.warn("RMLConnect login returned no JWTAUTH");
                return null;
            }
            cachedToken = response.jwtauth();
            // Documented as a 1-hour validity; refresh early rather than trust that exactly.
            cachedTokenExpiresAt = Instant.now().plusSeconds(3600 - REFRESH_MARGIN_SECONDS);
            return cachedToken;
        } catch (RestClientException e) {
            log.warn("RMLConnect login failed: {}", e.getMessage());
            return null;
        }
    }

    private record LoginRequest(String username, String password) {}

    private record LoginResponse(String JWTAUTH, Object user_data) {
        // Jackson matches field names case-sensitively by default; RML's response uses
        // these exact keys (see the docs) rather than the project's usual camelCase.
        String jwtauth() {
            return JWTAUTH;
        }
    }
}
