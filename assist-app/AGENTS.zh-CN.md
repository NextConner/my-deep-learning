# 仓库指南

## 项目结构与模块组织
`src/` 存放 React + TypeScript 客户端代码。`src/components/` 放置界面组件，例如 `Login.tsx` 和 `ChatInterface.tsx`；共享类型定义位于 `src/types.ts`；`src/main.tsx` 是浏览器入口，`src/App.tsx` 负责组装应用。`server.ts` 用于启动本地 Express 服务，并在开发环境中挂载 Vite 中间件。静态 HTML 文件位于 `index.html`。配置文件包括 `vite.config.ts`、`tsconfig.json` 和 `metadata.json`。本地密钥配置请以 `.env.example` 为模板。

## 构建、测试与开发命令
先执行 `npm install` 安装依赖。

- `npm run dev`：启动 Express + Vite 开发服务，默认端口为 `3000`。
- `npm run build`：构建生产版本，输出到 `dist/`。
- `npm run preview`：本地预览已构建的前端产物。
- `npm run lint`：运行 `tsc --noEmit` 做类型检查。

本地运行前，请在 `.env.local` 中设置 `GEMINI_API_KEY`。

## 编码风格与命名约定
使用 TypeScript，统一 2 个空格缩进，并保持现有分号风格。遵循当前命名方式：React 组件使用 PascalCase（如 `Watermark.tsx`），变量和函数使用 camelCase，共享导出类型集中放在 `src/types.ts`。优先使用函数式 React 组件，import 放在文件顶部。注释应尽量精简，仅用于解释不直观的逻辑。

## 测试指南
当前仓库尚未提交自动化测试套件。在新增测试体系前，`npm run lint` 和通过 `npm run dev` 进行手动验证应视为每次改动的最低检查要求。新增测试时，建议放在对应功能旁边，或放入专用目录 `src/__tests__/`，文件名使用 `*.test.ts` 或 `*.test.tsx`。

## 提交与 Pull Request 规范
近期提交历史以简短、聚焦任务的提交信息为主；如果一次提交包含多项相关改动，通常会使用编号，例如：`1.修复前端SSE事件解析错误 2.修改提示信息`。提交应尽量小而明确，并聚焦单一改动集合。创建 Pull Request 时，请提供简要说明、验证步骤、关联 issue，以及涉及界面改动时的截图或短视频。

## 安全与配置建议
不要提交 `.env.local` 或任何 API Key。示例配置保留在 `.env.example` 中；新增环境变量时，也应同步更新该示例文件并补充说明。
