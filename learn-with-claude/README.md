# learn-with-claude

一个基于 `Spring Boot + LangChain4j + PostgreSQL(pgvector)` 的企业知识助手示例项目，支持：
- JWT 登录鉴权
- 文档上传与切分（PDF/DOCX）
- 向量检索 + 关键词检索（Hybrid Search）
- Agent 工具调用（政策检索、天气工具）
- SSE 流式对话输出
- Token 用量统计与配额控制

## 技术栈

- Java 21
- Spring Boot 3.3.5
- LangChain4j 0.36.2
- PostgreSQL + pgvector
- Maven
- 前端：单页 `Vue 3`（静态页面）

## 目录结构

```text
src/main/java/com/claude/learn
  ├─ controller    # 鉴权、聊天、文档上传接口
  ├─ service       # 检索、文档入库、Token监控等
  ├─ agent         # Agent 定义与工具实现
  ├─ config        # Security / LangChain 配置
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

### 1) 数据库配置

项目会在启动时执行 `src/main/resources/schema.sql`，请先保证数据库可连接，并提前安装 pgvector：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

默认配置在 [application.yml](/D:/github/my-deep-learning/learn-with-claude/src/main/resources/application.yml)：
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

注意：`LangChainConfig` 中 `EmbeddingStore` 目前写死了数据库地址（`192.168.20.129:5432/ragdb`），如果你的环境不同，需要同步修改 [LangChainConfig.java](/D:/github/my-deep-learning/learn-with-claude/src/main/java/com/claude/learn/config/LangChainConfig.java)。

### 2) API Key 配置

项目读取以下环境变量：
- `DEEPSEEK_API_KEY`：对话模型 Key（DeepSeek）
- `EMBEDDING_API_KEY`：Embedding 模型 Key（DashScope 兼容接口）

PowerShell 示例：

```powershell
$env:DEEPSEEK_API_KEY="your_deepseek_key"
$env:EMBEDDING_API_KEY="your_embedding_key"
```

## 启动项目

```powershell
mvn clean spring-boot:run
```

启动后访问：
- 前端页面：`http://localhost:8080/index.html`
- 服务端口：`8080`

## 默认登录账号

当前登录逻辑为演示写法（硬编码）：
- 用户名：`admin`
- 密码：`123456`

参考 [AuthController.java](/D:/github/my-deep-learning/learn-with-claude/src/main/java/com/claude/learn/controller/AuthController.java)。

## 核心接口

### 1) 登录

`POST /api/auth/login`

```json
{
  "username": "admin",
  "password": "123456"
}
```

返回：

```json
{
  "token": "..."
}
```

### 2) 同步聊天

`POST /api/chat`（Header 需带 `Authorization: Bearer <token>`）

```json
{
  "message": "出差酒店能报销多少？"
}
```

### 3) 流式聊天（SSE）

`GET /api/chat/stream?message=xxx&token=xxx`

说明：SSE 场景中 token 通过 query 参数透传（见过滤器实现）。

### 4) 文档上传

`POST /api/document/upload`（multipart/form-data）
- 参数：`file`（支持 `.pdf`、`.docx`）
- Header：`Authorization: Bearer <token>`

### 5) Token 用量查询

`GET /api/usage`（Header 需带 Token）

## 典型使用流程

1. 启动服务并登录获取 JWT。
2. 上传企业政策文档（PDF/DOCX）。
3. 通过 `/api/chat` 或 `/api/chat/stream` 提问。
4. Agent 自动调用 `searchPolicy` / `getWeather` 工具返回结果。
5. 在 `/api/usage` 查看当日 Token 使用情况。

## 测试

项目包含多组集成测试（`src/test/java`），多数测试依赖：
- 可访问的数据库
- 有效的 API Key
- 可访问的模型服务

执行：

```powershell
mvn test
```

## 已知事项

- 当前包含示例性质的硬编码配置（数据库地址、默认账号）。
- `application.yml` 中示例账号和密码仅适用于本地开发，请勿用于生产环境。
- 文档上传接口当前只实现了 `.pdf` 和 `.docx` 解析。

## 后续可改进

1. 将数据库和模型配置全部改为环境变量，不在代码中写死。
2. 将演示登录替换为真实用户体系（DB + 密码加密）。
3. 为上传、检索、对话流程补充端到端测试和异常场景测试。
