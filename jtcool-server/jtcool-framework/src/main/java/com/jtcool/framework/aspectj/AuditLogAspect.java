package com.jtcool.framework.aspectj;

import com.jtcool.common.utils.SecurityUtils;
import com.jtcool.common.utils.ip.IpUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 审计日志切面
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger auditLog = LoggerFactory.getLogger("audit");

    @AfterReturning("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logAudit(JoinPoint joinPoint) {
        try {
            String username = SecurityUtils.getUsername();
            String ip = IpUtils.getIpAddr();
            String method = joinPoint.getSignature().toShortString();
            auditLog.info("User: {}, IP: {}, Operation: {}", username, ip, method);
        } catch (Exception e) {
            auditLog.error("Audit log failed", e);
        }
    }
}
