# 跨切面规则

## 项目概览

| 维度 | 说明 |
|------|------|
| 技术栈 | Java 21 + Spring Boot 3 + LangChain4j + PostgreSQL/pgvector |
| 前端 | React + TypeScript + Vite（assist-app/） |
| 后端 | learn-with-claude/（Maven 项目） |
| 主包名 | `com.claude.learn` |
| 启动类 | `DemoApplication.java` |

## 全局安全模式

`config/SecurityModeProperties.java` 控制两种鉴权模式：
- **本地 JWT 模式**（默认）：`/api/auth/login` → 颁发 JWT → `JwtAuthFilter` 校验
- **企业 JWT 模式**（`app.security.mode=enterprise`）：`EnterpriseJwtAuthFilter` 验外部 SSO token，`/api/auth/login` 被禁用

## Token 配额（全局前置检查）

所有 `/api/chat` 请求，在进入 Agent 之前必须通过 `TokenMonitorService.isExceeded(username)` 检查。
- 超额返回 HTTP 429 或 SSE `error` 事件，错误码 `QUOTA_EXCEEDED`
- 配额数据存储：`TokenUsage`（DB）+ `MapTokenCacheStrategy`（内存缓存）

## Virtual Thread

`ChatController` 和 `AgentOrchestratorService` 均使用 `Executors.newVirtualThreadPerTaskExecutor()`（Java 21）处理并发，适合高 IO 场景。

## 分布式追踪

- MDC：`traceId`（= `AgentRun.runId`）、`username` 注入每条日志
- Micrometer Tracer（Jaeger）：span name = `agent.orchestrator.run`

## 关键配置（application.yml 路径）

`learn-with-claude/src/main/resources/application.yml`

```yaml
app:
  models:
    primary:   # DeepSeek（主力）base-url: https://api.deepseek.com, model: deepseek-chat
    fallback:  # Ollama phi3:mini（本地路由 + 熔断降级用）base-url: http://localhost:11434/v1
    back:      # Claude claude-sonnet-4-6（备用）
  security:
    mode: local | enterprise
jwt:
  secret: ${JWT_SECRET:change-me-in-production}
  expiration: 86400000   # 24 小时（毫秒）
agent:
  runtime:
    max-steps: 3
    step-timeout-ms: 15000
    retry-times: 1
rag:
  chunk:
    max-segment-size: 300
    max-overlap-size: 30
```

## 错误码体系

`agent/runtime/AgentErrorCode.java`：`MAX_STEPS_EXCEEDED`、`TOOL_TIMEOUT`、`TOOL_EXECUTION_FAIL`

## 可重试条件

`AgentOrchestratorService.isRetryable()`：
- `SocketTimeoutException` ✓
- `TimeoutException` ✓
- message 含 "429" ✓
- 其他 ✗
