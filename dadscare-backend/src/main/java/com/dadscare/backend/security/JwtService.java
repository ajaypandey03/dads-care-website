package com.dadscare.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues and parses the JWTs that carry a user's identity + tenant across every
 * authenticated request. The JWT is the ONLY source of {@code organizationId} —
 * {@link JwtAuthFilter} populates {@link com.dadscare.backend.tenant.TenantContext}
 * from it, and callers must never accept an org id from a request body/path param
 * instead.
 */
@Component
public class JwtService {

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
    }

    public String issueAccessToken(Long userId, Long organizationId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("orgId", organizationId)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    /** @throws io.jsonwebtoken.JwtException if the token is missing, malformed, expired, or has a bad signature */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
