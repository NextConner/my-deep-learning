package com.claude.learn.service;

import com.claude.learn.domain.AuditLog;
import com.claude.learn.repository.AuditLogRepository;
import com.claude.learn.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);

    private final AuditLogRepository auditLogRepository;

    public SecurityAuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String traceId,
                       String action,
                       String resource,
                       String decision,
                       String reason,
                       Authentication authentication) {
        try {
            AuditLog entry = new AuditLog();
            entry.setTraceId(traceId == null || traceId.isBlank() ? UUID.randomUUID().toString() : traceId);
            entry.setAction(action);
            entry.setResource(resource);
            entry.setDecision(decision);
            entry.setReason(reason);

            if (authentication != null) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserPrincipal userPrincipal) {
                    entry.setUserId(userPrincipal.userId());
                    entry.setUsername(userPrincipal.username());
                    entry.setDeptId(userPrincipal.deptId());
                } else {
                    entry.setUserId(authentication.getName());
                    entry.setUsername(authentication.getName());
                }
            } else {
                entry.setUserId("anonymous");
                entry.setUsername("anonymous");
            }

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist security audit log: {}", e.getMessage());
        }
    }

    public void logDatabaseQuery(String username, String sql, int resultCount) {
        try {
            AuditLog entry = new AuditLog();
            entry.setTraceId(UUID.randomUUID().toString());
            entry.setAction("DATABASE_QUERY");
            entry.setResource(sql);
            entry.setDecision("ALLOWED");
            entry.setReason("Result count: " + resultCount);
            entry.setUsername(username);
            entry.setUserId(username);
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist database query audit log: {}", e.getMessage());
        }
    }
}
