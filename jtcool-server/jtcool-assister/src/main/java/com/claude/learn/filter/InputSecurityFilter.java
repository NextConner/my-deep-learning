package com.claude.learn.filter;

import com.claude.learn.config.SecurityGatewayProperties;
import com.claude.learn.service.SecurityAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@Component
public class InputSecurityFilter extends OncePerRequestFilter {

    private final SecurityGatewayProperties securityGatewayProperties;
    private final SecurityAuditService securityAuditService;

    public InputSecurityFilter(SecurityGatewayProperties securityGatewayProperties,
                               SecurityAuditService securityAuditService) {
        this.securityGatewayProperties = securityGatewayProperties;
        this.securityAuditService = securityAuditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!securityGatewayProperties.isInputEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();
        if (!path.startsWith("/api/chat")) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        String payload;
        HttpServletRequest wrappedRequest = request;
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
            wrappedRequest = cached;
            payload = cached.getCachedBodyAsString();
        } else {
            payload = request.getParameter("message");
        }
        if (payload == null) {
            payload = "";
        }

        if (payload.length() > securityGatewayProperties.getMaxInputLength()) {
            reject(response, traceId, path, "INPUT_TOO_LONG", "Input length exceeds security limit");
            return;
        }

        String lowerPayload = payload.toLowerCase(Locale.ROOT);
        for (String blocked : securityGatewayProperties.getBlockedPatterns()) {
            if (blocked != null && !blocked.isBlank() && lowerPayload.contains(blocked.toLowerCase(Locale.ROOT))) {
                reject(response, traceId, path, "PROMPT_INJECTION", "Input blocked by security policy");
                return;
            }
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private void reject(HttpServletResponse response,
                        String traceId,
                        String resource,
                        String reason,
                        String message) throws IOException {
        securityAuditService.record(
                traceId,
                "INPUT_CHECK",
                resource,
                "DENY",
                reason,
                SecurityContextHolder.getContext().getAuthentication()
        );
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"error\":\"" + message + "\",\"code\":\"" + reason + "\"}");
    }
}
