package com.claude.learn.controller;


import com.claude.learn.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 简化处理：固定用户名密码，生产环境应查数据库
        if ("admin".equals(request.username()) && "123456".equals(request.password())) {
            String token = jwtService.generateToken(request.username());
            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
    }

    public record LoginRequest(String username, String password) {}
}
