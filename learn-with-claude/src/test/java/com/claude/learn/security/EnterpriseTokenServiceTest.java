package com.claude.learn.security;

import com.claude.learn.config.EnterpriseIdentityProperties;
import com.claude.learn.config.SecurityModeProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnterpriseTokenServiceTest {

    private static final String SECRET = "test-secret-at-least-32-chars-long-123456";

    @Test
    void shouldFailFastWhenEnterpriseModeMissingIssuerOrAudience() {
        EnterpriseIdentityProperties identity = new EnterpriseIdentityProperties();
        identity.setJwtSecret(SECRET);

        SecurityModeProperties mode = new SecurityModeProperties();
        mode.setMode("enterprise-jwt");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new EnterpriseTokenService(identity, mode));

        assertTrue(ex.getMessage().contains("security.identity.issuer"));
    }

    @Test
    void shouldParsePrincipalWithConfiguredClaims() {
        EnterpriseIdentityProperties identity = new EnterpriseIdentityProperties();
        identity.setJwtSecret(SECRET);
        identity.setIssuer("corp-idp");
        identity.setAudience("ai-assistant");

        SecurityModeProperties mode = new SecurityModeProperties();
        mode.setMode("enterprise-jwt");

        EnterpriseTokenService service = new EnterpriseTokenService(identity, mode);

        String token = Jwts.builder()
                .issuer("corp-idp")
                .audience().add("ai-assistant").and()
                .claim("sub", "u-1001")
                .claim("preferred_username", "alice")
                .claim("dept_id", "RND")
                .claim("roles", List.of("AI_ADMIN", "AI_OPS"))
                .claim("data_scopes", "policy,finance")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        UserPrincipal principal = service.parsePrincipal(token);

        assertEquals("u-1001", principal.userId());
        assertEquals("alice", principal.username());
        assertTrue(principal.roles().contains("AI_ADMIN"));
        assertTrue(principal.dataScopes().contains("policy"));
    }
}
