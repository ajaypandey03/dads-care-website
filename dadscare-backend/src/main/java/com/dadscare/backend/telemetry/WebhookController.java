package com.dadscare.backend.telemetry;

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
 * Receives Velosyss's outbound lock-event push. Not JWT-authenticated (Velosyss is not
 * a logged-in user) — authenticated instead by {@link WebhookSignatureVerifier}, checked
 * against the raw request body before it's parsed. See SecurityConfig for why this path
 * is excluded from the JWT filter chain, and the Integration Contract page in Confluence
 * for the full wire contract.
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
    public ResponseEntity<?> receiveLockEvents(
            @RequestBody byte[] rawBody, @RequestHeader(value = "X-Signature", required = false) String signature) {

        if (!signatureVerifier.isValid(rawBody, signature)) {
            log.warn("Rejected webhook push: missing/invalid X-Signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_signature"));
        }

        LockEventBatch batch;
        try {
            batch = objectMapper.readValue(rawBody, LockEventBatch.class);
        } catch (Exception e) {
            log.warn("Rejected webhook push: malformed payload", e);
            return ResponseEntity.badRequest().body(Map.of("error", "malformed_payload"));
        }

        var violations = validator.validate(batch);
        if (!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "validation_failed", "details", violations.toString()));
        }

        WebhookService.IngestResult result = webhookService.ingest(batch);
        log.info(
                "Webhook batch processed: accepted={}, duplicates={}, unknownDevices={}",
                result.accepted(),
                result.duplicates(),
                result.unknownDevices());
        return ResponseEntity.ok(result);
    }
}
