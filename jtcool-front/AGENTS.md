# Repository Guidelines

## Project Structure & Module Organization
This repository is the Vue 3 frontend for JtCool, built with Vite and Element Plus. Main application code lives in `src/`. Use `src/views/` for page-level features, `src/api/` for request wrappers, `src/components/` for reusable UI, `src/store/` for Pinia state, `src/router/` for route definitions, and `src/utils/` for shared helpers. Static assets belong in `src/assets/`, while public files that should be served as-is belong in `public/`. Build-related Vite helpers are kept in `vite/`.

## Build, Test, and Development Commands
Install dependencies with `yarn --registry=https://registry.npmmirror.com` or `npm install`.

- `yarn dev`: start the Vite dev server.
- `yarn build:prod`: build the production bundle.
- `yarn build:stage`: build using the staging mode from `.env.staging`.
- `yarn preview`: serve the built bundle locally for verification.

Environment files are stored at the repository root as `.env.development`, `.env.staging`, and `.env.production`.

## Coding Style & Naming Conventions
Follow the existing JavaScript + Vue Single File Component style in `src/`: 2-space indentation, semicolon-free statements, and single quotes by default. Keep page folders and route modules grouped by domain, for example `src/views/system/` and `src/api/system/`. Use PascalCase for shared component files such as `ImageUpload.vue`, camelCase for utilities, and descriptive API module names such as `login.js` or `menu.js`.

This repository does not currently include a committed ESLint or Prettier config, so match surrounding code closely and keep imports ordered and grouped as existing files do.

## Testing Guidelines
There is no automated test framework wired into `package.json` yet. Before opening a PR, run `yarn build:prod` and smoke-test the affected flows in `yarn dev`. If you add tests, place them near the feature or under a top-level `tests/` directory, and use `*.spec.js` naming so they are easy to discover.

## Commit & Pull Request Guidelines
Recent history uses short, direct commit subjects, often in Chinese, for example `优化字典类型属性提醒说明`. Keep commits focused on one change. PRs should include a clear summary, impacted modules, linked issues, and screenshots or GIFs for UI changes. Call out any environment or backend dependency needed to verify the change.

## Configuration Notes
Do not commit real credentials into `.env.*` files. Frontend behavior depends on the matching JtCool backend API, so document any new endpoints, permissions, or dictionary keys in the PR description.
