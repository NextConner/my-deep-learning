package com.claude.learn.filter;

import com.claude.learn.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static io.prometheus.metrics.model.snapshots.Exemplar.TRACE_ID;

/**
 * 一次性身份验证过滤器
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    //注入
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        log.info("🔍 请求路径：{}", path);
        MDC.put(TRACE_ID, UUID.randomUUID().toString().replace("-", ""));
        //放行登录接口和静态资源
        if (path.equals("/index.html")
                || path.equals("/")
                || path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveBearerToken(request);

        //验证token
        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        //token 合法，设置认证信息到SecurityContext（如果需要）
        String userName = jwtService.extractUsername(token);
        Set<SimpleGrantedAuthority> authorities = "admin".equalsIgnoreCase(userName)
                ? Set.of(new SimpleGrantedAuthority("ROLE_AI_ADMIN"), new SimpleGrantedAuthority("ROLE_AI_OPS"))
                : Set.of(new SimpleGrantedAuthority("ROLE_AI_USER"));

        UsernamePasswordAuthenticationToken authToke = new UsernamePasswordAuthenticationToken(
                userName, null, authorities
        );
        authToke.setDetails(new WebAuthenticationDetailsSource()
                .buildDetails(request));
        //设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authToke);

        filterChain.doFilter(request,response);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        // SSE 等场景浏览器无法设置自定义 header，允许从 query param 传 token
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isBlank()) {
            return queryToken;
        }
        return null;
    }

}
