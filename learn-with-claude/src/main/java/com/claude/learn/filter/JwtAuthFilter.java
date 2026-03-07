package com.claude.learn.filter;

import com.claude.learn.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 一次性身份验证过滤器
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    //注入
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //放行登录接口
        if(request.getServletPath().startsWith("/api/auth")){
            filterChain.doFilter(request,response);
            return;
        }

        //从Header 获取token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        //提取token
        String token = authHeader.substring(7);

        //验证token
        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
            return;
        }

        //token 合法，设置认证信息到SecurityContext（如果需要）
        String userName = jwtService.extractUsername(token);
        UsernamePasswordAuthenticationToken authToke = new UsernamePasswordAuthenticationToken(
                userName,null, List.of()
        );
        authToke.setDetails(new WebAuthenticationDetailsSource()
                .buildDetails(request));
        //设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authToke);

        filterChain.doFilter(request,response);
    }

}
