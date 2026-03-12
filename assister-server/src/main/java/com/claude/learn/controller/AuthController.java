package com.claude.learn.controller;


import com.claude.learn.config.SecurityModeProperties;
import com.claude.learn.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final SecurityModeProperties securityModeProperties;
    private final String enterpriseName;

    public AuthController(
            JwtService jwtService,
            SecurityModeProperties securityModeProperties,
            @Value("${app.enterprise.name:企业}") String enterpriseName
    ) {
        this.jwtService = jwtService;
        this.securityModeProperties = securityModeProperties;
        this.enterpriseName = enterpriseName;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (securityModeProperties.isEnterpriseJwtMode()) {
            return ResponseEntity.status(405).body(Map.of("error", "登录端点已禁用"));
        }

        // 简化处理：固定用户名密码，生产环境应查数据库
        if ("admin".equals(request.username()) && "123456".equals(request.password())) {
            String token = jwtService.generateToken(request.username());
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
    }

    @GetMapping("/enterprise-info")
    public ResponseEntity<?> getEnterpriseInfo() {
        return ResponseEntity.ok(Map.of(
                "name", enterpriseName,
                "assistantName", enterpriseName + "AI助手"
        ));
    }

    public record LoginRequest(String username, String password) {}
}
