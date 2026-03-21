# Repository Guidelines

## Project Structure & Module Organization

This repository contains several related applications:

- `jtcool-server/`: Java 21 Maven multi-module backend. `jtcool-assister/` contains the AI service, while `jtcool-admin/`, `jtcool-framework/`, `jtcool-system/`, `jtcool-common/`, `jtcool-quartz/`, and `jtcool-generator/` hold shared platform modules.
- `jtcool-front/`: Vue 3 + Vite admin frontend under `src/`, static files in `public/`.
- `assist-app/`: React 19 + Vite app with TypeScript sources in `src/` and a local Express entry in `server.ts`.
- `logs/` and `jtcool-server/sql/`: runtime logs and SQL bootstrap scripts.

Keep changes scoped to the owning app. More specific notes exist in nested `AGENTS.md` files.

## Build, Test, and Development Commands

- `cd jtcool-server && mvn clean compile`: compile all backend modules.
- `cd jtcool-server && mvn -pl jtcool-assister spring-boot:run`: run the AI service on port `8080`.
- `cd jtcool-server && mvn test`: run backend tests.
- `cd jtcool-front && npm run dev`: start the Vue admin app.
- `cd jtcool-front && npm run build:prod`: produce the production bundle.
- `cd assist-app && npm run dev`: run the React app via `tsx server.ts`.
- `cd assist-app && npm run lint`: TypeScript type-check only.

## Coding Style & Naming Conventions

Follow the style already present in each module. Java uses 4-space indentation, `UpperCamelCase` classes, `lowerCamelCase` methods, and package paths under `com.jtcool` or `com.claude.learn`. Vue and React files use PascalCase component names such as `ChatInterface.tsx` and `RightToolbar.vue`; utility modules stay lowercase. Prefer existing `@/` import aliases in frontend code. No root formatter config is enforced, so keep diffs minimal and consistent with nearby files.

## Testing Guidelines

Backend tests live in `jtcool-server/jtcool-assister/src/test/java` and use Spring Boot Test with JUnit-style `*Test`/`*Tests` naming. Add tests beside the feature you change, especially for controller, service, security, and agent orchestration behavior. Frontend projects currently do not define automated test scripts, so at minimum run the relevant build or type-check command before opening a PR.

## Commit & Pull Request Guidelines

Recent history uses short, task-focused messages, often in Chinese, for example `1.修复编译问题` and `清洗项目结构`. Keep commits concise, imperative, and tied to one change. PRs should describe the affected module, list verification commands, link related issues, and include screenshots for UI changes. Call out config, schema, or API contract updates explicitly.

## Security & Configuration Tips

Do not commit credentials. Start from `assist-app/.env.example` and review `jtcool-server/jtcool-assister/src/main/resources/application.yml` before local runs. Database and model-provider settings are environment-sensitive; document any new required keys in the relevant module README.
