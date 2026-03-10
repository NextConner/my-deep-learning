# 身份与鉴权集成改造清单（Spring Boot 3.3.5 + Java 21）

> 目标：在当前 `JwtAuthFilter + SecurityConfig + ChatController` 基础上，平滑集成企业统一身份（OIDC/网关透传）与细粒度鉴权（RBAC + 数据域权限）。

## 1. 改造原则（按优先级）

1. **兼容现有链路**：保留 `/api/chat`、`/api/chat/stream` 接口与现有 `SecurityFilterChain`。
2. **先认证后鉴权**：先把“谁在调用”变成可信，再补“能调用什么”。
3. **最小权限**：默认拒绝，按角色/数据域逐步放开。
4. **可审计**：每次调用必须可追踪到用户、角色、部门、工具、结果。
5. **灰度切换**：保留本地 JWT 开关用于开发环境，生产使用企业 IdP。

---

## 2. 分层设计（沿用现有目录）

```text
config/
  SecurityConfig.java                   # 安全链路总装配
  SecurityModeProperties.java           # security.mode = local-jwt | enterprise-jwt
  MethodSecurityConfig.java             # 开启 @EnableMethodSecurity

filter/
  JwtAuthFilter.java                    # 现有过滤器，保留
  EnterpriseJwtAuthFilter.java          # 新增：企业 token 验签 + claims 映射
  SecurityAuditFilter.java              # 新增：审计日志补充过滤器

security/
  EnterpriseTokenVerifier.java          # 新增：JWK/Introspection 校验器
  ClaimsToPrincipalMapper.java          # 新增：claims -> UserPrincipal
  DataScopeEvaluator.java               # 新增：数据权限判断
  PermissionEvaluatorImpl.java          # 新增：方法级权限扩展
  UserPrincipal.java                    # 新增：userId/deptId/roles/dataScopes

controller/
  AuthController.java                   # 保留，仅 dev profile 允许 login
  ChatController.java                   # 读取 principal（替换裸 username）
  ToolExecutionController.java          # 增加 @PreAuthorize

service/
  JwtService.java                       # 仅 local-jwt 使用
  IdentityContextService.java           # 新增：统一取当前身份与数据域
  AuthorizationService.java             # 新增：业务级鉴权（工具、知识库、租户）
  AuditLogService.java                  # 新增：落审计日志

repository/
  IdentityMappingRepository.java        # 新增：用户角色/数据域映射（可选）
  AuditLogRepository.java               # 新增：审计日志落库

resources/
  application.yml                       # 增加 security.identity.* 配置
  schema.sql                            # 增加审计表 / 身份映射表（可选）
```

---

## 3. 配置改造（application.yml）

新增配置建议（示例）：

```yaml
security:
  mode: ${SECURITY_MODE:enterprise-jwt}   # local-jwt | enterprise-jwt
  identity:
    issuer-uri: ${SECURITY_IDENTITY_ISSUER_URI:}
    jwk-set-uri: ${SECURITY_IDENTITY_JWK_SET_URI:}
    audience: ${SECURITY_IDENTITY_AUDIENCE:ai-assistant}
    username-claim: ${SECURITY_USERNAME_CLAIM:preferred_username}
    user-id-claim: ${SECURITY_USER_ID_CLAIM:sub}
    dept-id-claim: ${SECURITY_DEPT_ID_CLAIM:dept_id}
    roles-claim: ${SECURITY_ROLES_CLAIM:roles}
    data-scopes-claim: ${SECURITY_DATA_SCOPES_CLAIM:data_scopes}
```

> 注意：当前 `spring.spring.security.strategy` 建议校正为 `spring.security.strategy`，避免配置无效。

---

## 4. 关键类骨架（可直接建类）

## 4.1 `security/UserPrincipal.java`

```java
package com.claude.learn.security;

import java.util.Set;

public record UserPrincipal(
        String userId,
        String username,
        String deptId,
        Set<String> roles,
        Set<String> dataScopes
) {}
```

## 4.2 `security/EnterpriseTokenVerifier.java`

```java
package com.claude.learn.security;

import io.jsonwebtoken.Claims;

public interface EnterpriseTokenVerifier {
    Claims verifyAndParse(String rawToken);
}
```

> 实现类可做两种策略：
> - OIDC JWK 本地验签
> - 调企业网关 introspection

## 4.3 `security/ClaimsToPrincipalMapper.java`

```java
package com.claude.learn.security;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ClaimsToPrincipalMapper {

    public UserPrincipal map(Claims claims) {
        String userId = (String) claims.get("sub");
        String username = (String) claims.getOrDefault("preferred_username", userId);
        String deptId = (String) claims.get("dept_id");
        Set<String> roles = extractSet(claims, "roles");
        Set<String> scopes = extractSet(claims, "data_scopes");
        return new UserPrincipal(userId, username, deptId, roles, scopes);
    }

    private Set<String> extractSet(Claims claims, String key) {
        Object value = claims.get(key);
        // TODO: 支持 List<String> / 逗号分隔字符串
        return Set.of();
    }
}
```

## 4.4 `filter/EnterpriseJwtAuthFilter.java`

```java
package com.claude.learn.filter;

import com.claude.learn.security.ClaimsToPrincipalMapper;
import com.claude.learn.security.EnterpriseTokenVerifier;
import com.claude.learn.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.stream.Collectors;

public class EnterpriseJwtAuthFilter extends OncePerRequestFilter {

    private final EnterpriseTokenVerifier verifier;
    private final ClaimsToPrincipalMapper mapper;

    public EnterpriseJwtAuthFilter(EnterpriseTokenVerifier verifier, ClaimsToPrincipalMapper mapper) {
        this.verifier = verifier;
        this.mapper = mapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String raw = request.getHeader("Authorization");
        if (raw == null || !raw.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = raw.substring(7);
        Claims claims = verifier.verifyAndParse(token);
        UserPrincipal principal = mapper.map(claims);

        var authorities = principal.roles().stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }
}
```

## 4.5 `service/IdentityContextService.java`

```java
package com.claude.learn.service;

import com.claude.learn.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class IdentityContextService {

    public UserPrincipal requirePrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal p)) {
            throw new IllegalStateException("No authenticated enterprise principal");
        }
        return p;
    }

    public String currentUsername() {
        return requirePrincipal().username();
    }
}
```

## 4.6 `service/AuthorizationService.java`

```java
package com.claude.learn.service;

import com.claude.learn.security.UserPrincipal;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public void checkToolPermission(UserPrincipal principal, String toolName) {
        // TODO: 按角色 + 数据域 + 工具白名单校验
    }

    public void checkKnowledgeScope(UserPrincipal principal, String source) {
        // TODO: 保证检索文档 source 在 dataScopes 范围内
    }
}
```

---

## 5. 现有类的改造点（逐文件）

## 5.1 `SecurityConfig`

- 增加 `SecurityModeProperties` 注入。
- `security.mode=enterprise-jwt` 时挂载 `EnterpriseJwtAuthFilter`。
- `security.mode=local-jwt` 时继续使用 `JwtAuthFilter`。
- `web.ignoring()` 建议仅保留静态资源，API 全部走过滤器链。

## 5.2 `JwtAuthFilter`

- 仅 dev/local 场景启用。
- 去掉 query 参数 token（避免 URL 泄露），SSE 统一走 header。
- 抽公共方法：`resolveBearerToken(HttpServletRequest)`。

## 5.3 `AuthController`

- `@Profile("local")` 或 `security.mode=local-jwt` 才暴露 `/login`。
- enterprise 模式返回 404/405，避免误用本地登录。

## 5.4 `ChatController`

- 当前 `getCurrentUsername()` 改为从 `IdentityContextService` 获取 `UserPrincipal`。
- `tokenMonitorService` 统计维度建议从 `username` 扩展为 `tenant/userId`。

## 5.5 `ToolExecutionController` / `AgentTools`

- 给高风险工具增加 `@PreAuthorize("hasAnyRole('AI_ADMIN','AI_OPS')")`。
- 工具执行前调用 `authorizationService.checkToolPermission(...)`。

---

## 6. 数据库与审计表建议

在 `schema.sql` 增量增加：

```sql
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGSERIAL PRIMARY KEY,
    trace_id        VARCHAR(64),
    user_id         VARCHAR(128) NOT NULL,
    username        VARCHAR(128) NOT NULL,
    dept_id         VARCHAR(128),
    action          VARCHAR(64) NOT NULL,
    resource        VARCHAR(256),
    decision        VARCHAR(16) NOT NULL,   -- ALLOW / DENY
    reason          TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_user_created
ON audit_log (user_id, created_at DESC);
```

可选（如果不直接信任 token claims）：

```sql
CREATE TABLE IF NOT EXISTS identity_mapping (
    user_id         VARCHAR(128) PRIMARY KEY,
    username        VARCHAR(128) NOT NULL,
    dept_id         VARCHAR(128),
    roles           TEXT,
    data_scopes     TEXT,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. 上线步骤（建议 3 个迭代）

## Iteration 1（1 周）

- 新增 `security.mode` 配置与 enterprise filter（只做认证，不拦业务）。
- 日志中输出 principal 基本信息（userId/username/deptId）。
- 保证 `/api/chat` 与 `/api/chat/stream` 在企业 token 下可用。

## Iteration 2（1~2 周）

- 接入 `@EnableMethodSecurity` + `@PreAuthorize`。
- 对工具和文档上传接口加角色控制。
- 落审计日志（ALLOW/DENY + reason）。

## Iteration 3（1~2 周）

- 数据域鉴权：RAG 检索按 `dataScopes` 过滤。
- Tool 执行增加“角色 + 数据域 + 风险等级”联合校验。
- 回归压测与灰度发布。

---

## 8. 测试用例清单（最少）

1. **认证成功**：携带合法企业 token 请求 `/api/chat` 返回 200。
2. **认证失败**：过期/伪造 token 返回 401。
3. **角色拒绝**：无 `AI_ADMIN` 调用管理接口返回 403。
4. **数据域拒绝**：非授权部门访问受限文档返回 403。
5. **SSE 认证**：`/api/chat/stream` 使用 header token 正常连通。
6. **审计落库**：ALLOW 和 DENY 均有记录。

---

## 9. 风险与回滚

- **风险 1**：企业 token claims 不稳定（字段名变化）。
  - 对策：claims 字段全部配置化（`security.identity.*-claim`）。
- **风险 2**：切 enterprise 后本地联调困难。
  - 对策：保留 `local-jwt` 模式与 profile，隔离生产。
- **风险 3**：数据域策略误拦截。
  - 对策：先“只审计不拦截”一周，再启用强拦截。

---

## 10. 最终落地标准（DoD）

- 生产环境禁止使用本地 `/api/auth/login`。
- 所有业务 API 具备可信身份（principal 非匿名）。
- 高风险接口具备方法级鉴权。
- 工具与检索具备数据域控制。
- 审计日志可追踪到用户、操作、决策、原因。

