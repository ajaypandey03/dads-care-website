package com.dadscare.backend.telemetry;

import com.dadscare.backend.velosyss.VelosyssWebhookEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receives Velosyss's outbound push — one event per {@code POST}, per §4 of the
 * "Velosyss Lock Integration Guide" in Confluence. Not JWT-authenticated (Velosyss is
 * not a logged-in user) — authenticated instead by {@link WebhookSignatureVerifier},
 * checked against the raw request body before it's parsed. See SecurityConfig for why
 * this path is excluded from the JWT filter chain.
 *
 * <p>Per §4.4: respond quickly and return 2xx as soon as the event is durably accepted
 * (we ingest synchronously here, which is fast enough at Dad's Care's current volume —
 * revisit with an accept-and-queue split if that changes) — a non-2xx or timeout gets
 * retried up to twice, but a 4xx is treated as a <em>permanent</em> rejection, so we only
 * ever return 4xx for a genuinely bad request (bad signature, malformed/invalid body),
 * never for a business-level "nothing to do" case like an unrecognized device or a
 * duplicate — those still return 200.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/velosyss")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookSignatureVerifier signatureVerifier;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(value = "/lock-events", consumes = "application/json")
    public ResponseEntity<?> receiveLockEvent(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Velosyss-Signature", required = false) String signature) {

        if (!signatureVerifier.isValid(rawBody, signature)) {
            log.warn("Rejected webhook push: missing/invalid X-Velosyss-Signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_signature"));
        }

        VelosyssWebhookEvent event;
        try {
            event = objectMapper.readValue(rawBody, VelosyssWebhookEvent.class);
        } catch (Exception e) {
            log.warn("Rejected webhook push: malformed payload", e);
            return ResponseEntity.badRequest().body(Map.of("error", "malformed_payload"));
        }

        var violations = validator.validate(event);
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "validation_failed", "details", violations.toString()));
        }

        WebhookService.IngestOutcome outcome = webhookService.ingest(event);
        log.info("Webhook event processed: eventId={}, eventType={}, outcome={}", event.eventId(), event.eventType(), outcome);
        return ResponseEntity.ok(Map.of("outcome", outcome));
    }
}
