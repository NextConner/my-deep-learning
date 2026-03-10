package com.claude.learn.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ToolPolicyGuardService {

    public void checkToolAccess(String toolName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if ("sendEmail".equals(toolName) && !hasAnyRole(authentication, "ROLE_AI_ADMIN")) {
            throw new AccessDeniedException("Tool access denied: sendEmail requires AI_ADMIN role");
        }
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            for (String role : roles) {
                if (role.equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }
}
