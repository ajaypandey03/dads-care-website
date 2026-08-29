package com.dadscare.backend.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class WebhookSignatureVerifierTest {

    private static final String SECRET = "test-shared-secret";
    private final WebhookSignatureVerifier verifier = new WebhookSignatureVerifier(SECRET);

    @Test
    void acceptsACorrectlySignedBody() throws Exception {
        byte[] body = "{\"events\":[]}".getBytes(StandardCharsets.UTF_8);
        String signature = sign(body, SECRET);

        assertThat(verifier.isValid(body, signature)).isTrue();
    }

    @Test
    void rejectsATamperedBody() throws Exception {
        byte[] originalBody = "{\"events\":[]}".getBytes(StandardCharsets.UTF_8);
        String signature = sign(originalBody, SECRET);
        byte[] tamperedBody = "{\"events\":[1]}".getBytes(StandardCharsets.UTF_8);

        assertThat(verifier.isValid(tamperedBody, signature)).isFalse();
    }

    @Test
    void rejectsASignatureFromTheWrongSecret() throws Exception {
        byte[] body = "{\"events\":[]}".getBytes(StandardCharsets.UTF_8);
        String signature = sign(body, "a-different-secret");

        assertThat(verifier.isValid(body, signature)).isFalse();
    }

    @Test
    void rejectsAMissingSignature() {
        byte[] body = "{\"events\":[]}".getBytes(StandardCharsets.UTF_8);

        assertThat(verifier.isValid(body, null)).isFalse();
        assertThat(verifier.isValid(body, "")).isFalse();
    }

    @Test
    void rejectsANonHexSignature() {
        byte[] body = "{\"events\":[]}".getBytes(StandardCharsets.UTF_8);

        assertThat(verifier.isValid(body, "sha256=not-hex-at-all")).isFalse();
    }

    @Test
    void failsClosedWhenNoSecretIsConfigured() throws Exception {
        WebhookSignatureVerifier unconfigured = new WebhookSignatureVerifier("");
        byte[] body = "{\"events\":[]}".getBytes(StandardCharsets.UTF_8);
        String signature = sign(body, SECRET);

        assertThat(unconfigured.isValid(body, signature)).isFalse();
    }

    private static String sign(byte[] body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(body);
        StringBuilder hex = new StringBuilder();
        for (byte b : raw) {
            hex.append(String.format("%02x", b));
        }
        return "sha256=" + hex;
    }
}
