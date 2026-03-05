package com.example.aiagent.config;

import com.example.aiagent.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户过滤器
 * 从请求头中提取租户ID并设置到上下文，实现多租户隔离
 */
@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

    /** 请求头中租户ID的键名 */
    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // 从请求头获取租户ID
        String tenantId = request.getHeader(TENANT_HEADER);
        
        if (tenantId != null && !tenantId.isEmpty()) {
            // 设置租户上下文
            TenantContext.setTenantId(tenantId);
            log.debug("设置租户上下文: {}", tenantId);
        } else {
            // 开发环境使用默认租户
            TenantContext.setTenantId("default");
            log.debug("使用默认租户");
        }
        
        try {
            // 继续执行过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 请求完成后清理租户上下文，防止线程复用导致数据泄露
            TenantContext.clear();
        }
    }
}
