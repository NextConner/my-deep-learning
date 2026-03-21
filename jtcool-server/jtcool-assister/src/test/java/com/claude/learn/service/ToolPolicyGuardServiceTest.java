package com.claude.learn.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ToolPolicyGuardServiceTest {

    private final ToolPolicyGuardService service = new ToolPolicyGuardService();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldDenySendEmailForNonAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "u1", null, Set.of(new SimpleGrantedAuthority("ROLE_AI_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThrows(AccessDeniedException.class, () -> service.checkToolAccess("sendEmail"));
    }

    @Test
    void shouldAllowSendEmailForAdmin() {
        var auth = new UsernamePasswordAuthenticationToken(
                "admin", null, Set.of(new SimpleGrantedAuthority("ROLE_AI_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertDoesNotThrow(() -> service.checkToolAccess("sendEmail"));
    }
}
