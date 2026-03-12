# Auth 单元 — Overview

## 单元摘要

双模式认证子系统：本地 JWT 模式（简化 demo 账号）和企业 JWT 模式（外部 SSO token 直通），通过 `SecurityModeProperties` 运行时切换，`SecurityConfig` 控制路由白名单。

## 需求背景

企业场景需要支持两种认证方式：
1. **本地模式**：快速 demo，固定 admin/123456 账号
2. **企业模式**：接入公司 SSO，外部系统颁发 JWT，本系统直接验证

## 关键代码

| 文件 | 说明 |
|------|------|
| `controller/AuthController.java:23` | `POST /api/auth/login` |
| `filter/JwtAuthFilter.java` | 本地 JWT 校验 Filter |
| `filter/EnterpriseJwtAuthFilter.java` | 企业 SSO JWT 校验 Filter |
| `filter/InputSecurityFilter.java` | 输入内容安全过滤（XSS、注入防护）|
| `filter/SecurityAuditFilter.java` | 请求审计日志 |
| `service/JwtService.java` | JWT 生成与校验 |
| `config/SecurityConfig.java` | Spring Security 路由白名单 |
| `config/SecurityModeProperties.java` | `app.security.mode` 配置 |
| `config/SecurityGatewayProperties.java` | 企业网关相关配置 |
| `config/UserContext.java` | 当前用户上下文持有 |
| `security/UserPrincipal.java` | `record UserPrincipal(String username)` |

## 入口与边界

- **白名单**（不需要认证）：`/api/auth/**`、`/actuator/**`、`/v3/api-docs/**`
- **保护范围**：`/api/**`（除白名单）
- **Token 传递**：HTTP header `Authorization: Bearer <token>` 或 query param `token=<jwt>`

## 本地 JWT 登录流程

```
POST /api/auth/login  { "username": "admin", "password": "123456" }
  → AuthController.login()
  → securityModeProperties.isEnterpriseJwtMode() == false
  → 硬编码比对 admin/123456
  → jwtService.generateToken(username)
  → 返回 { "token": "<jwt>" }
```

## JWT 校验流程（本地模式）

```
请求到达 JwtAuthFilter（OncePerRequestFilter）
  → 从 header / query param 提取 Bearer token
  → jwtService.validate(token)
  → 构建 UsernamePasswordAuthenticationToken
  → SecurityContextHolder.setContext(...)
  → 继续过滤链
```

## 企业 JWT 模式

```
app.security.mode=enterprise
  → EnterpriseJwtAuthFilter 生效（JwtAuthFilter 禁用）
  → POST /api/auth/login 返回 405
  → 外部 SSO 颁发的 token 直接被 EnterpriseJwtAuthFilter 校验
  → 从 token claims 中提取 username 写入 SecurityContext
```

## 安全过滤链顺序

```
InputSecurityFilter（输入净化）
  → SecurityAuditFilter（审计日志）
  → JwtAuthFilter / EnterpriseJwtAuthFilter（认证）
  → Spring Security 授权检查
  → Controller
```

## 规则与约束

- 演示账号 admin/123456 硬编码，**不适合生产**
- `UserPrincipal` 是 record，`ChatController.getCurrentUsername()` 通过 `instanceof UserPrincipal` 获取 username
- 企业模式下 `LocalQueryRouter` 仍可正常使用（不涉及认证逻辑）

## 风险与未知项

- 无密码加密（明文比对），生产必须改为数据库查询 + BCrypt
- **[Author's analysis]** `CachedBodyHttpServletRequest.java` 用于允许 `InputSecurityFilter` 多次读取 request body（Spring 默认只能读一次）
- JWT secret 需通过环境变量注入，不应 hardcode
