# Repository Guidelines

## Project Structure & Module Organization
This repository uses a Maven multi-module backend and a separate Vue 2 frontend. Backend modules live at the root: `ruoyi-admin` is the Spring Boot entrypoint, `ruoyi-framework` holds security/config infrastructure, `ruoyi-system` contains core business services and mappers, `ruoyi-common` provides shared utilities and domain models, `ruoyi-quartz` manages scheduled jobs, and `ruoyi-generator` contains code generation templates. Frontend code lives in `ruoyi-ui/src`, with API clients under `src/api`, routed pages under `src/views`, reusable layout/components under `src/components` and `src/layout`, and static assets under `src/assets`. SQL bootstrap files are in `sql/`; helper scripts are in `bin/`.

## Build, Test, and Development Commands
Run backend tasks from the repository root:

- `mvn clean package -DskipTests` builds all Java modules and produces the `ruoyi-admin` artifact.
- `mvn -pl ruoyi-admin spring-boot:run` starts the backend locally.
- `mvn test` runs Maven tests when a module includes them.

Run frontend tasks from `ruoyi-ui/`:

- `npm install` installs UI dependencies.
- `npm run dev` starts the Vue development server.
- `npm run build:prod` creates a production bundle.
- `npm run preview` serves the built frontend for a quick verification pass.

## Coding Style & Naming Conventions
Use 4 spaces in Java and 2 spaces in Vue/JavaScript files. Keep Java packages under `com.ruoyi.<module>`; use `PascalCase` for classes, `camelCase` for methods/fields, and suffix Spring components clearly, for example `SysUserController`, `SysUserServiceImpl`, and `SysUserMapper`. In the UI, keep page components in `src/views` with descriptive lowercase folder names and `index.vue` entry files where the feature already follows that pattern. Preserve existing MyBatis XML mapper names and paths under `src/main/resources/mapper`.

## Testing Guidelines
There is no large, dedicated test suite in the current tree. Add backend tests under `src/test/java` beside the module you change, and prefer focused service or controller tests for bug fixes. For frontend changes, document manual verification steps and exercise the affected screen with `npm run dev` before opening a PR.

## Commit & Pull Request Guidelines
Recent history uses short, imperative commit subjects, often in Chinese, such as `升级fastjson到最新版2.0.61` or `remove test code`. Keep subjects concise, scoped to one change, and avoid mixing backend and frontend refactors unless they ship together. PRs should explain the problem, summarize the fix, list verification commands, link related issues, and include screenshots for UI changes.

## Configuration & Security Tips
Do not commit secrets or environment-specific edits to `ruoyi-admin/src/main/resources/application.yml`. Keep local database or Redis overrides out of version control, and update `sql/*.sql` only when schema changes are intentional and reviewed.
