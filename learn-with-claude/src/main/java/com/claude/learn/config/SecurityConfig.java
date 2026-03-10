package com.claude.learn.config;

import com.claude.learn.filter.EnterpriseJwtAuthFilter;
import com.claude.learn.filter.JwtAuthFilter;
import com.claude.learn.filter.SecurityAuditFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final EnterpriseJwtAuthFilter enterpriseJwtAuthFilter;
    private final SecurityModeProperties securityModeProperties;
    private final SecurityAuditFilter securityAuditFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          EnterpriseJwtAuthFilter enterpriseJwtAuthFilter,
                          SecurityModeProperties securityModeProperties,
                          SecurityAuditFilter securityAuditFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.enterpriseJwtAuthFilter = enterpriseJwtAuthFilter;
        this.securityModeProperties = securityModeProperties;
        this.securityAuditFilter = securityAuditFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 完全绕过 Security 过滤器链，不走 JwtAuthFilter
        return web -> web.ignoring()
                .requestMatchers("/index.html", "/", "/*.html", "/static/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()  // 登录接口放行
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()                  // 其余全部需要认证
                )
                .addFilterBefore(resolveAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(securityAuditFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }


    private OncePerRequestFilter resolveAuthFilter() {
        if (securityModeProperties.isEnterpriseJwtMode()) {
            return enterpriseJwtAuthFilter;
        }
        return jwtAuthFilter;
    }

}
