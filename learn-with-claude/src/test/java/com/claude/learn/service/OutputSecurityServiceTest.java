package com.claude.learn.service;

import com.claude.learn.config.SecurityGatewayProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputSecurityServiceTest {

    @Test
    void shouldMaskSensitiveContent() {
        SecurityGatewayProperties properties = new SecurityGatewayProperties();
        OutputSecurityService service = new OutputSecurityService(properties);

        String raw = "联系方式: 13812345678, 邮箱: user@test.com";
        String sanitized = service.sanitize(raw);

        assertNotEquals(raw, sanitized);
        assertTrue(sanitized.contains("[REDACTED]"));
    }
}
