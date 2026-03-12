# Repository Guidelines

## 项目结构与模块组织
本仓库是 JtCool 的 Vue 3 前端，基于 Vite 和 Element Plus 构建。主要业务代码位于 `src/`。页面级功能放在 `src/views/`，接口请求封装放在 `src/api/`，通用组件放在 `src/components/`，Pinia 状态管理位于 `src/store/`，路由定义位于 `src/router/`，公共工具放在 `src/utils/`。静态资源放在 `src/assets/`，需要原样发布的文件放在 `public/`，Vite 相关构建辅助代码位于 `vite/`。

## 构建、测试与开发命令
使用 `yarn --registry=https://registry.npmmirror.com` 或 `npm install` 安装依赖。

- `yarn dev`：启动 Vite 本地开发服务。
- `yarn build:prod`：构建生产环境产物。
- `yarn build:stage`：按 `.env.staging` 配置构建预发环境。
- `yarn preview`：本地预览已构建产物。

环境变量文件位于仓库根目录：`.env.development`、`.env.staging`、`.env.production`。

## 代码风格与命名规范
遵循 `src/` 现有的 JavaScript 与 Vue 单文件组件风格：2 空格缩进、默认不写分号、优先使用单引号。页面目录与接口模块按业务域组织，例如 `src/views/system/` 与 `src/api/system/`。共享组件文件使用 PascalCase，例如 `ImageUpload.vue`；工具函数使用 camelCase；接口模块使用语义明确的命名，例如 `login.js`、`menu.js`。

仓库当前未提交 ESLint 或 Prettier 配置，因此修改时应以周边代码为准，并保持现有 import 分组和排序风格一致。

## 测试指南
当前 `package.json` 尚未接入自动化测试框架。提交 PR 前，至少执行 `yarn build:prod`，并通过 `yarn dev` 手动验证受影响流程。如果新增测试，建议放在对应功能附近或顶层 `tests/` 目录，并使用 `*.spec.js` 命名，便于统一发现和维护。

## 提交与 Pull Request 规范
最近提交历史以简短、直接的标题为主，例如 `优化字典类型属性提醒说明`。每次提交应聚焦单一改动。PR 需要包含变更摘要、影响模块、关联 Issue；涉及界面调整时，附上截图或 GIF。若验证依赖特定环境变量、后端接口或权限配置，也需要在 PR 描述中明确说明。

## 配置说明
不要在 `.env.*` 文件中提交真实凭据。前端行为依赖配套的 JtCool 后端接口，若新增接口、权限标识或字典项，请在 PR 中同步说明，便于联调和验收。
