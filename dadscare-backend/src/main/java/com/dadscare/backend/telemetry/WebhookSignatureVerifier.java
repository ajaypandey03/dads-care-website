package com.dadscare.backend.telemetry;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Verifies the HMAC-SHA256 signature Velosyss's Outbound Integration Service puts on
 * every push, per the Integration Contract in Confluence. One shared secret for the
 * whole integration — Velosyss only knows Dad's Care as a single Enterprise account, so
 * this is a platform-level secret, not one per Dad's Care {@code Organization}. Which
 * organization a given event belongs to is resolved afterward, from the device.
 */
@Component
public class WebhookSignatureVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secretKeyBytes;

    public WebhookSignatureVerifier(@Value("${app.webhook.velosyss-secret:}") String secret) {
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    private static final String SIGNATURE_PREFIX = "sha256=";

    /**
     * @param rawBody the exact, unparsed request body bytes — signatures are computed over
     *                the raw bytes, never the re-serialized object, so parsing must happen
     *                after this check, not before.
     * @param providedSignatureHeader the value of the {@code X-Velosyss-Signature} header
     *                — {@code "sha256=<hex>"} per §4.3. A bare hex string (no prefix) is
     *                also accepted, for local testing against the raw HMAC.
     */
    public boolean isValid(byte[] rawBody, String providedSignatureHeader) {
        if (providedSignatureHeader == null || providedSignatureHeader.isBlank()) {
            return false;
        }
        if (secretKeyBytes.length == 0) {
            // No secret configured (e.g. local dev without app.webhook.velosyss-secret set) —
            // fail closed rather than silently accepting unsigned requests.
            return false;
        }
        String hex = providedSignatureHeader.startsWith(SIGNATURE_PREFIX)
                ? providedSignatureHeader.substring(SIGNATURE_PREFIX.length())
                : providedSignatureHeader;
        byte[] expected = computeHmac(rawBody);
        byte[] provided = hexDecode(hex);
        if (provided == null) {
            return false;
        }
        return java.security.MessageDigest.isEqual(expected, provided);
    }

    private byte[] computeHmac(byte[] body) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGORITHM));
            return mac.doFinal(body);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private byte[] hexDecode(String hex) {
        try {
            int len = hex.length();
            if (len % 2 != 0) {
                return null;
            }
            byte[] out = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
            return out;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
