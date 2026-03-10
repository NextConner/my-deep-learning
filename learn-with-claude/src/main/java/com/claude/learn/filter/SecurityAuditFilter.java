package com.claude.learn.filter;

import com.claude.learn.service.SecurityAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class SecurityAuditFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-Id";

    private final SecurityAuditService securityAuditService;

    public SecurityAuditFilter(SecurityAuditService securityAuditService) {
        this.securityAuditService = securityAuditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/index.html") || path.equals("/") || path.startsWith("/static/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            String decision = status < 400 ? "ALLOW" : "DENY";
            String reason = "HTTP_" + status;

            securityAuditService.record(
                    traceId,
                    request.getMethod(),
                    path,
                    decision,
                    reason,
                    SecurityContextHolder.getContext().getAuthentication()
            );
        }
    }
}
