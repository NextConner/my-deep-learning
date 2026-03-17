package com.claude.learn.service;

import com.claude.learn.config.SecurityGatewayProperties;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class OutputSecurityService {

    private final SecurityGatewayProperties securityGatewayProperties;

    public OutputSecurityService(SecurityGatewayProperties securityGatewayProperties) {
        this.securityGatewayProperties = securityGatewayProperties;
    }

    public String sanitize(String output) {
        if (output == null || output.isBlank() || !securityGatewayProperties.isOutputEnabled()) {
            return output;
        }

        String sanitized = output;
        for (String regex : securityGatewayProperties.getSensitivePatterns()) {
            if (regex == null || regex.isBlank()) {
                continue;
            }
            sanitized = Pattern.compile(regex).matcher(sanitized).replaceAll("[REDACTED]");
        }
        return sanitized;
    }
}
