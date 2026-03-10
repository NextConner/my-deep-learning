package com.claude.learn.security;

import com.claude.learn.config.EnterpriseIdentityProperties;
import com.claude.learn.config.SecurityModeProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class EnterpriseTokenService {

    private final EnterpriseIdentityProperties properties;
    private final SecurityModeProperties securityModeProperties;

    public EnterpriseTokenService(EnterpriseIdentityProperties properties,
                                  SecurityModeProperties securityModeProperties) {
        this.properties = properties;
        this.securityModeProperties = securityModeProperties;
        validateRequiredIdentityConfig();
    }

    public UserPrincipal parsePrincipal(String token) {
        Claims claims = parseClaims(token);
        validateIssuer(claims);
        validateAudience(claims);

        String userId = valueAsString(claims.get(properties.getUserIdClaim()));
        String username = valueAsString(claims.get(properties.getUsernameClaim()));
        String deptId = valueAsString(claims.get(properties.getDeptIdClaim()));

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("Enterprise token missing user id claim: " + properties.getUserIdClaim());
        }
        if (username == null || username.isBlank()) {
            username = userId;
        }

        Set<String> roles = extractSet(claims.get(properties.getRolesClaim()));
        Set<String> dataScopes = extractSet(claims.get(properties.getDataScopesClaim()));

        return new UserPrincipal(userId, username, deptId, roles, dataScopes);
    }

    private void validateRequiredIdentityConfig() {
        if (!securityModeProperties.isEnterpriseJwtMode()) {
            return;
        }
        if (properties.getIssuer() == null || properties.getIssuer().isBlank()) {
            throw new IllegalStateException("security.identity.issuer is required when security.mode=enterprise-jwt");
        }
        if (properties.getAudience() == null || properties.getAudience().isBlank()) {
            throw new IllegalStateException("security.identity.audience is required when security.mode=enterprise-jwt");
        }
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateIssuer(Claims claims) {
        if (properties.getIssuer() == null || properties.getIssuer().isBlank()) {
            return;
        }
        if (!properties.getIssuer().equals(claims.getIssuer())) {
            throw new IllegalArgumentException("Enterprise token issuer mismatch");
        }
    }

    private void validateAudience(Claims claims) {
        if (properties.getAudience() == null || properties.getAudience().isBlank()) {
            return;
        }
        if (claims.getAudience() == null || !claims.getAudience().contains(properties.getAudience())) {
            throw new IllegalArgumentException("Enterprise token audience mismatch");
        }
    }

    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Set<String> extractSet(Object value) {
        if (value == null) {
            return Set.of();
        }
        if (value instanceof Collection<?> c) {
            LinkedHashSet<String> out = new LinkedHashSet<>();
            for (Object item : c) {
                if (item != null) {
                    String text = String.valueOf(item).trim();
                    if (!text.isEmpty()) {
                        out.add(text);
                    }
                }
            }
            return out;
        }

        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return Set.of();
        }

        String[] parts = text.split(",");
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out;
    }
}
