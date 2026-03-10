# learn-with-claude

一个基于 `Spring Boot + LangChain4j + PostgreSQL(pgvector)` 的企业知识助手示例项目，支持：

- 文档上传与切分（PDF/DOCX）
- 向量检索 + 关键词检索（Hybrid Search）
- Agent 工具调用（政策检索、天气工具）
- SSE 流式对话输出
- Token 用量统计与配额控制
- 双认证模式：`local-jwt` / `enterprise-jwt`
- 方法级鉴权（`@PreAuthorize`，高风险接口限权）
- 安全审计日志（`audit_log`）

## 技术栈

- Java 21
- Spring Boot 3.3.5
- LangChain4j 0.36.2
- PostgreSQL + pgvector
- Maven

## 目录结构

```text
src/main/java/com/claude/learn
  ├─ controller    # 鉴权、聊天、文档上传接口
  ├─ service       # 检索、文档入库、Token监控等
  ├─ agent         # Agent 定义与工具实现
  ├─ config        # Security / LangChain / 运行配置
  ├─ filter        # JWT 过滤器
  ├─ security      # 企业身份 principal / token 解析
  └─ repository    # JPA 仓储
src/main/resources
  ├─ application.yml
  ├─ schema.sql
  └─ static/index.html
```

## 环境要求

1. JDK 21
2. Maven 3.9+
3. PostgreSQL 14+（已安装 `pgvector` 扩展）
4. 可用的 LLM / Embedding API Key

## 配置说明

## 1) 数据库配置

项目启动时会执行 `src/main/resources/schema.sql`。请先保证数据库可连接，并提前安装 pgvector：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

主要配置：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## 2) 模型配置

- `DEEPSEEK_API_KEY`：对话模型 Key
- `EMBEDDING_API_KEY`：Embedding 模型 Key

## 3) 认证模式配置（新增）

通过 `security.mode` 选择认证模式：

```yaml
security:
  mode: ${SECURITY_MODE:local-jwt} # local-jwt | enterprise-jwt
  identity:
    issuer: ${SECURITY_IDENTITY_ISSUER:}
    audience: ${SECURITY_IDENTITY_AUDIENCE:}
    username-claim: ${SECURITY_USERNAME_CLAIM:preferred_username}
    user-id-claim: ${SECURITY_USER_ID_CLAIM:sub}
    dept-id-claim: ${SECURITY_DEPT_ID_CLAIM:dept_id}
    roles-claim: ${SECURITY_ROLES_CLAIM:roles}
    data-scopes-claim: ${SECURITY_DATA_SCOPES_CLAIM:data_scopes}
    jwt-secret: ${SECURITY_ENTERPRISE_JWT_SECRET:change-enterprise-secret-in-production}
```

- `local-jwt`：使用本地 `JwtAuthFilter + JwtService`。
- `enterprise-jwt`：使用 `EnterpriseJwtAuthFilter + EnterpriseTokenService`，将 claims 映射为 `UserPrincipal`。

> 说明：当前 enterprise 方案为可运行版本，基于共享密钥验签（HMAC）。后续可替换为 JWK / introspection。

## 启动项目

```bash
mvn clean spring-boot:run
```

启动后访问：

- 前端页面：`http://localhost:8080/index.html`
- 服务端口：`8080`

## 登录与调用

## A. local-jwt（开发模式）

1) 登录获取 token

`POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "123456"
}
```

2) 调用聊天接口

`POST /api/chat`（Header 带 `Authorization: Bearer <token>`）

## B. enterprise-jwt（企业模式）

- 不使用本地登录接口（`/api/auth/login` 会返回 405）。
- 由企业网关/IdP 下发 token，客户端直接带 `Authorization: Bearer <token>` 调用业务接口。

## 核心接口

- `POST /api/auth/login`（仅 local-jwt 有意义）
- `POST /api/chat`
- `GET /api/chat/stream?message=...`（Header 带 `Authorization: Bearer <token>`）
- `POST /api/document/upload`（multipart/form-data，需 `AI_ADMIN` 或 `AI_OPS`）
- `GET /api/usage`
- `GET /api/tools/health` / `dashboard` / `recommendations`（需 `AI_ADMIN` 或 `AI_OPS`）
- `POST /api/tools/{toolName}/reset-stats`（需 `AI_ADMIN`）

## SSE 鉴权说明

- 统一使用 `Authorization` Header 传递 token。
- 出于安全考虑，已移除 query 参数 token 透传能力，避免 token 出现在 URL 与访问日志中。


## 安全网关（Phase A）

当前已实现三段式安全网关：

- **输入网关**：`InputSecurityFilter`（`/api/chat*`）
  - 限制输入最大长度（`security.gateway.max-input-length`）
  - 拦截 Prompt Injection 关键模式（`security.gateway.blocked-patterns`）
- **业务执行网关**：`ToolPolicyGuardService`
  - 在工具执行前做权限判断（当前 `sendEmail` 仅 `AI_ADMIN`）
- **输出网关**：`OutputSecurityService`
  - 对模型输出做敏感信息脱敏（`security.gateway.sensitive-patterns`）

可通过配置开关：

- `security.gateway.input-enabled`
- `security.gateway.output-enabled`

## 测试

```bash
mvn test
```

> 注意：多数测试依赖可访问的数据库与模型服务。

## 已知事项

- `AuthController` 的固定账号密码仅用于本地演示，请勿用于生产。
- 当前 enterprise token 仍为共享密钥验签（HMAC），建议下一步替换为 JWK / introspection。
- 数据域访问控制（按 `dataScopes` 过滤检索与工具调用）仍需在后续迭代补齐。

## 参考

- 企业身份集成计划：`SECURITY_IDENTITY_INTEGRATION_PLAN.md`
