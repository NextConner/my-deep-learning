package com.claude.learn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "security.gateway")
public class SecurityGatewayProperties {

    private boolean inputEnabled = true;
    private boolean outputEnabled = true;
    private int maxInputLength = 4000;

    private List<String> blockedPatterns = new ArrayList<>(List.of(
            "ignore previous instructions",
            "reveal system prompt",
            "print system prompt",
            "bypass security"
    ));

    private List<String> sensitivePatterns = new ArrayList<>(List.of(
            "\\b1[3-9]\\d{9}\\b",
            "\\b[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]\\b",
            "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"
    ));

    public boolean isInputEnabled() {
        return inputEnabled;
    }

    public void setInputEnabled(boolean inputEnabled) {
        this.inputEnabled = inputEnabled;
    }

    public boolean isOutputEnabled() {
        return outputEnabled;
    }

    public void setOutputEnabled(boolean outputEnabled) {
        this.outputEnabled = outputEnabled;
    }

    public int getMaxInputLength() {
        return maxInputLength;
    }

    public void setMaxInputLength(int maxInputLength) {
        this.maxInputLength = maxInputLength;
    }

    public List<String> getBlockedPatterns() {
        return blockedPatterns;
    }

    public void setBlockedPatterns(List<String> blockedPatterns) {
        this.blockedPatterns = blockedPatterns;
    }

    public List<String> getSensitivePatterns() {
        return sensitivePatterns;
    }

    public void setSensitivePatterns(List<String> sensitivePatterns) {
        this.sensitivePatterns = sensitivePatterns;
    }
}
