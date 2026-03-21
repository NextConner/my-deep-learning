package com.claude.learn.config;

import com.claude.learn.filter.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.DispatcherType;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final EnterpriseJwtAuthFilter enterpriseJwtAuthFilter;
    private final SecurityModeProperties securityModeProperties;
    private final SecurityAuditFilter securityAuditFilter;
    private final InputSecurityFilter inputSecurityFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          EnterpriseJwtAuthFilter enterpriseJwtAuthFilter,
                          SecurityModeProperties securityModeProperties,
                          SecurityAuditFilter securityAuditFilter,
                          InputSecurityFilter inputSecurityFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.enterpriseJwtAuthFilter = enterpriseJwtAuthFilter;
        this.securityModeProperties = securityModeProperties;
        this.securityAuditFilter = securityAuditFilter;
        this.inputSecurityFilter = inputSecurityFilter;
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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()  // SSE 异步派发放行
                        .requestMatchers("/api/auth/**").permitAll()  // 登录接口放行
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()                  // 其余全部需要认证
                )
                .addFilterBefore(resolveAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(inputSecurityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(securityAuditFilter, InputSecurityFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private OncePerRequestFilter resolveAuthFilter() {
        if (securityModeProperties.isEnterpriseJwtMode()) {
            return enterpriseJwtAuthFilter;
        }
        return jwtAuthFilter;
    }

    /**
     * 禁止两个 Auth Filter 被 Spring Boot 自动注册为 Servlet 过滤器。
     * 它们只应通过 Spring Security 链（filterChain）按模式选一个运行，
     * 否则未选中的那个也会在 Security 链外额外执行，导致 401。
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<EnterpriseJwtAuthFilter> enterpriseFilterRegistration(EnterpriseJwtAuthFilter filter) {
        FilterRegistrationBean<EnterpriseJwtAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

}
