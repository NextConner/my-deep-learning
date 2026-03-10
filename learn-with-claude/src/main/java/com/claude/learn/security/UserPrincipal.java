package com.claude.learn.security;

import java.util.Set;

public record UserPrincipal(
        String userId,
        String username,
        String deptId,
        Set<String> roles,
        Set<String> dataScopes
) {
}
