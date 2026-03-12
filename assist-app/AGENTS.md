# Repository Guidelines

## Project Structure & Module Organization
`src/` contains the React + TypeScript client. `src/components/` holds UI pieces such as `Login.tsx` and `ChatInterface.tsx`; shared types live in `src/types.ts`; `src/main.tsx` is the browser entry and `src/App.tsx` wires the app together. `server.ts` runs the local Express server and mounts Vite middleware in development. Static HTML lives at `index.html`. Configuration is kept in `vite.config.ts`, `tsconfig.json`, and `metadata.json`. Use `.env.example` as the starting point for local secrets.

## Build, Test, and Development Commands
Install dependencies with `npm install`.

- `npm run dev`: starts the Express + Vite development server on port `3000`.
- `npm run build`: creates a production bundle in `dist/`.
- `npm run preview`: serves the built frontend for a quick production check.
- `npm run lint`: runs `tsc --noEmit` for type-checking.

Set `GEMINI_API_KEY` in `.env.local` before running locally.

## Coding Style & Naming Conventions
Use TypeScript with 2-space indentation and keep existing semicolon usage. Follow the current component naming pattern: PascalCase for React components (`Watermark.tsx`), camelCase for variables and functions, and concise exported types in `src/types.ts`. Prefer functional React components and colocate imports at the top of each file. Keep comments sparse and only for non-obvious logic.

## Testing Guidelines
There is no committed automated test suite yet. Until one is added, treat `npm run lint` and a manual check through `npm run dev` as the minimum validation for every change. When adding tests, place them next to the feature or under a dedicated `src/__tests__/` folder, and use `*.test.ts` or `*.test.tsx` naming.

## Commit & Pull Request Guidelines
Recent history uses short, task-focused commit messages, often numbered when bundling related changes, for example: `1.修复前端SSE事件解析错误 2.修改提示信息`. Keep commits small, descriptive, and scoped to one change set. For pull requests, include a brief summary, validation steps, linked issues, and screenshots or short recordings for UI changes.

## Security & Configuration Tips
Do not commit `.env.local` or API keys. Keep sample values in `.env.example`, and document any new environment variables there when adding integrations.
