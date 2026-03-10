package com.claude.learn.controller;

import com.claude.learn.config.SecurityModeProperties;
import com.claude.learn.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void shouldReturn405WhenEnterpriseModeLoginCalled() {
        JwtService jwtService = mock(JwtService.class);
        SecurityModeProperties mode = new SecurityModeProperties();
        mode.setMode("enterprise-jwt");

        AuthController controller = new AuthController(jwtService, mode);
        ResponseEntity<?> response = controller.login(new AuthController.LoginRequest("admin", "123456"));

        assertEquals(405, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertTrue(String.valueOf(body.get("error")).contains("disabled"));
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldAllowLocalModeLogin() {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generateToken("admin")).thenReturn("local-token");

        SecurityModeProperties mode = new SecurityModeProperties();
        mode.setMode("local-jwt");

        AuthController controller = new AuthController(jwtService, mode);
        ResponseEntity<?> response = controller.login(new AuthController.LoginRequest("admin", "123456"));

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("local-token", body.get("token"));
    }
}
