package com.dadscare.backend.security;

import com.dadscare.backend.tenant.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Reads {@code Authorization: Bearer <jwt>}, validates it, and populates both Spring
 * Security's context (for {@code @PreAuthorize}/role checks) and
 * {@link TenantContext} (for repository-level tenant scoping) for the duration of the
 * request. Always clears {@link TenantContext} in a {@code finally} block — thread
 * pool reuse means a stale value here would leak one request's tenant into another's.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        try {
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtService.parse(header.substring(7));
                Long userId = Long.valueOf(claims.getSubject());
                Long organizationId = ((Number) claims.get("orgId")).longValue();
                String role = claims.get("role", String.class);
                boolean platformAdmin = Boolean.TRUE.equals(claims.get("platformAdmin", Boolean.class));

                TenantContext.set(organizationId, userId, role, platformAdmin);

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } finally {
            TenantContext.clear();
        }
    }
}
