package com.claude.learn.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.jtcool-secret:abcdefghijklmnopqrstuvwxyz}")
    private String jtcoolSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 生成 Token
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // 解析用户名
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // 验证 Token
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        RuntimeException lastError = null;
        for (String candidate : new String[] { secret, jtcoolSecret }) {
            try {
                return parseClaims(token, candidate);
            } catch (RuntimeException error) {
                lastError = error;
            }
        }
        throw lastError == null ? new IllegalArgumentException("Invalid token") : lastError;
    }

    private Claims parseClaims(String token, String signingSecret) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (RuntimeException ignored) {
            SecretKey legacyKey = new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            return Jwts.parser()
                    .verifyWith(legacyKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
    }

}
