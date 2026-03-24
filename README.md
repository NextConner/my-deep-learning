# dev-deep-learning

企业内部 AI 项目（后端 + 前端配套）的工程化实践仓库。

- 后端：`jtcool-server`（Java 21 + Spring Boot 3.x + MyBatis）
  - `jtcool-assister`：AI 助手服务（LangChain4j 0.36.2）
  - `jtcool-oms`：订单管理系统
  - `jtcool-wms`：仓库管理系统
  - `jtcool-product`：产品档案管理
- 前端：`jtcool-front`（Vue 3 + Element Plus）

> 说明：当前仓库包含完整的ERP系统（OMS/WMS/产品档案）和AI助手服务。

---

## 1. 项目能力概览

### AI 助手服务（jtcool-assister）
- RAG（向量检索 + 关键词检索 Hybrid Search）
- 多 Agent 编排与 Plan 模式
- 动态 Prompt / 配置化多模型加载
- Token 配额与观测能力
- 双鉴权模式：`local-jwt` / `enterprise-jwt`
- 安全网关（Phase A）：输入检测、工具权限网关、输出脱敏
- 安全审计日志（`audit_log`）

### ERP 核心模块
- **OMS（订单管理系统）**：客户管理、订单管理、财务管理、订单统计
- **WMS（仓库管理系统）**：仓库管理、库存管理、出入库单管理、库存统计
- **产品档案管理**：品牌管理、分类管理、产品信息管理

---

## 2. 目录结构

```text
dev-deep-learning/
├─ jtcool-server/
│  ├─ jtcool-assister/           # AI 助手服务（Spring Boot + LangChain4j）
│  ├─ jtcool-oms/                # 订单管理系统
│  ├─ jtcool-wms/                # 仓库管理系统
│  ├─ jtcool-product/            # 产品档案管理
│  ├─ jtcool-common/             # 公共模块
│  ├─ jtcool-framework/          # 框架模块
│  ├─ jtcool-system/             # 系统管理
│  ├─ jtcool-admin/              # 后台管理入口
│  └─ pom.xml                    # Maven 聚合工程
├─ jtcool-front/                 # Vue3 前端项目
└─ README.md                     # 仓库总说明（本文件）
```

---

## 3. 技术栈（基于 pom.xml + 前端实现）

## 后端（jtcool-assister）

- Java 21
- Spring Boot 3.3.5
- LangChain4j 0.36.2
- Spring Security + JJWT 0.12.3
- Spring Data JPA + PostgreSQL + pgvector
- Resilience4j
- Micrometer + Prometheus + Zipkin

## 前端（assist-app / 内置页面）

- Vue 3（当前内置页面为 CDN 方式）
- `fetch` 调用 REST API
- SSE（`EventSource`）流式展示回答
- `marked`（Markdown 渲染）

---

## 4. 前后端集成契约（建议 assist-app 遵循）

后端默认对外接口前缀：`/api`

核心接口：

- `POST /api/auth/login`（仅 `local-jwt` 有意义）
- `POST /api/chat`
- `GET /api/chat/stream?message=...`（SSE）
- `POST /api/document/upload`
- `GET /api/usage`

鉴权约定：

- 统一使用 `Authorization: Bearer <token>`
- enterprise 模式下，`/api/auth/login` 会返回 `405`

---

## 5. 本地运行（后端）

在 `jtcool-server` 目录执行：

```bash
mvn clean spring-boot:run
```

默认端口：`8080`

数据库：PostgreSQL（需安装 pgvector 扩展）

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

---

## 6. 配置要点

配置文件：`jtcool-server/jtcool-assister/src/main/resources/application.yml`

关键配置分组：

- `spring.datasource.*`：数据库
- `app.models.*`：多模型路由
- `security.mode`：`local-jwt | enterprise-jwt`
- `security.identity.*`：企业身份映射
- `security.gateway.*`：输入/输出安全网关

---

## 7. assist-app 对接建议

如果 `assist-app` 为独立前端工程，建议按以下方式对齐：

1. **统一环境变量命名**（例如后端 base URL、SSE URL、鉴权模式标记）
2. **统一错误码约定**（特别是 401/403/405/429）
3. **SSE 统一 token 策略**（Header 透传）
4. **输出渲染前二次安全处理**（例如前端 XSS 安全渲染策略）
5. **联调用例固定化**（登录、流式问答、文档上传、工具权限拒绝）

---

## 8. 开发与测试

### 后端测试

项目采用TDD（测试驱动开发）模式，已为核心模块添加单元测试：

**OMS模块测试**：
```bash
cd jtcool-server/jtcool-oms
mvn test
```

测试覆盖：
- 订单CRUD操作
- 订单状态更新
- 客户管理

**运行所有测试**：
```bash
cd jtcool-server
mvn test
```

> 若环境无法访问 Maven Central / 外部模型服务，部分测试与构建会受限。

---

## 9. 进一步阅读

- 后端详细说明：`jtcool-server/jtcool-assister/README.md`
- 企业身份集成计划：`jtcool-server/jtcool-assister/SECURITY_IDENTITY_INTEGRATION_PLAN.md`
