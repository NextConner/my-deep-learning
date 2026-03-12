# Frontend 单元 — Overview

## 单元摘要

React + TypeScript 前端（assist-app/），包含登录页、聊天界面、水印组件，通过 SSE 接收流式答案，有一个轻量 Node.js 代理服务器（server.ts）解决跨域问题。

## 需求背景

用户需要在浏览器中登录并与企业知识助手对话，支持实时流式显示 Agent 执行步骤和答案。

## 关键代码

| 文件 | 说明 |
|------|------|
| `assist-app/src/App.tsx` | 路由根组件，管理 token 状态，Login ↔ ChatInterface 切换 |
| `assist-app/src/components/Login.tsx` | 登录表单，调用 `/api/auth/login` |
| `assist-app/src/components/ChatInterface.tsx` | 聊天主界面，SSE 处理逻辑 |
| `assist-app/src/components/Watermark.tsx` | 用户名水印组件 |
| `assist-app/src/types.ts` | 类型定义 |
| `assist-app/server.ts` | Express 代理服务器（开发/部署用）|
| `assist-app/vite.config.ts` | Vite 配置，代理后端接口 |

## 组件层次

```
App
├─ Login（未登录时）
│   └─ POST /api/auth/login → 存储 token（localStorage/state）
└─ ChatInterface（已登录时）
    ├─ Watermark（用户名浮层）
    └─ SSE 消息列表
```

## SSE 事件处理（ChatInterface）

**重要**：前端使用 `fetch()` + `ReadableStream` 方式接收 SSE，**不是** `EventSource`（这样可以设置 Authorization header）。

```typescript
// ChatInterface.tsx:470-477
const url = `/api/chat/stream?message=${encodeURIComponent(text)}`;
const response = await fetch(url, {
  headers: { 'Authorization': `Bearer ${user.token || ''}` }
});

// 手动解析 SSE 流（ChatInterface.tsx:522-545）
// 解析 "event: xxx\ndata: yyy\n\n" 格式
```

**SSE 事件处理**（processSseEvent，行 557-621）：

| 事件 | 处理逻辑 |
|------|---------|
| `token` | 逐字符累加到 fullContent，更新消息 UI |
| `complete` | 解析 JSON，获取最终 answer，更新状态 |
| `error` | 显示红色错误消息（`isError: true`）|
| `step_start` / `step_complete` | 忽略（前端不展示步骤详情）|

## 登录流程

```
用户输入 username/password
  → POST /api/auth/login
  → 成功：token + username 存储到 localStorage
  → 切换到 ChatInterface
  → 失败：显示错误提示
```

## 前端校验规则（ChatInterface）

- 每个会话最多 **20 条**助理回复（行 389）
- 输入最大 **500 字**（行 406）
- 敏感词列表（行 13）：`['暴力', '色情', '赌博', '毒品', '脏话', '骂人', '攻击性言论']`

## Token 传递方式

SSE 请求通过 `fetch()` + `Authorization: Bearer <token>` header 传递（非 query param）。

## 代理服务器（server.ts）

- 用途：生产环境将前端静态文件与后端 API 代理整合，避免跨域
- 端口：通常 `3000`
- 代理规则：`/api/*` → `http://localhost:8080`

## 风险与未知项

- **[Author's analysis]** token 存储方式（localStorage vs sessionStorage）需确认，localStorage 存在 XSS 风险
- `EventSource` 不支持 POST，因此 stream 接口必须是 GET，消息通过 query param 传递（URL 长度有限制）
- 前端无 token 刷新机制，JWT 过期后需重新登录
