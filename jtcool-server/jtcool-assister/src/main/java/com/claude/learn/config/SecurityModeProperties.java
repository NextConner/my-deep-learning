package com.claude.learn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security")
public class SecurityModeProperties {

    /**
     * local-jwt | enterprise-jwt
     */
    private String mode = "local-jwt";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isEnterpriseJwtMode() {
        return "enterprise-jwt".equalsIgnoreCase(mode);
    }
}

