# Java AI Agent Monolith (Spring Boot)

单体项目骨架：**RAG + Agent 工具调用 + 企业治理（多租户/RBAC/审计/限流）**。

## 运行前提

- **JDK 21**（推荐；你当前机器已装 JDK 24 也可先用）
- Maven 3.9+
- PostgreSQL 15+（需要 `pgvector` 扩展）

> 注意：你当前机器上 `mvn -v` 可能仍在使用 JDK 8。Spring Boot 3.5 需要 JDK 17+，本项目使用 JDK 21。

### 让 Maven 使用 JDK 21

在 PowerShell 中临时设置（当前窗口有效）：

```powershell
$env:JAVA_HOME="C:\\Program Files\\Java\\jdk-21"
$env:Path="$env:JAVA_HOME\\bin;$env:Path"
mvn -v
```

确保 `mvn -v` 输出的 `Java version` 是 21+。

如果你机器上暂时只有 JDK 24（本机检测到 `C:\\Program Files\\Java\\jdk-24`），也可以先这样跑通构建：

```powershell
$env:JAVA_HOME="C:\\Program Files\\Java\\jdk-24"
$env:Path="$env:JAVA_HOME\\bin;$env:Path"
mvn -v
mvn -DskipTests package
```

## 本地启动（开发模式）

1) 启动数据库（或自行使用现有 PG）并确保启用 `pgvector`：

- 你可以在 PG 中执行：
  - `CREATE EXTENSION IF NOT EXISTS vector;`

2) 配置环境变量（示例）：

```powershell
$env:LLM_PROVIDER="openai"     # openai / deepseek
$env:OPENAI_API_KEY="..."
$env:DEEPSEEK_API_KEY="..."
```

3) 启动：

```powershell
mvn spring-boot:run
```

打开：
- Actuator: `http://localhost:8080/actuator/health`

## 当前包含的接口（占位骨架）

- `POST /api/chat`
- `POST /api/documents`

## 下一步你可以实现的核心模块

- `service/llm`: `LlmGatewayService`（统一模型调用、超时重试、token/成本统计）
- `service/rag`: 文档分块、embedding、pgvector 检索、引用返回
- `service/agent`: tool/function calling + agent loop
- `service/governance`: 多租户、RBAC、审计、限流/预算

