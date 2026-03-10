# Repository Guidelines

## Project Structure & Module Organization
This is a Java 21 Spring Boot project using LangChain4j and PostgreSQL/pgvector.

- `src/main/java/com/claude/learn`: application code
- `controller/`: REST endpoints (`/api/auth`, `/api/chat`, `/api/document`)
- `service/`: orchestration, RAG ingest/search, JWT, token monitoring
- `agent/`: agent roles, tool wrappers, runtime/invoker abstractions
- `config/`, `filter/`, `domain/`, `repository/`: app configuration, auth filter, entities, JPA repositories
- `src/main/resources`: `application.yml`, `schema.sql`, static page (`static/index.html`), sample docs
- `src/test/java/com/claude/learn`: integration-style and service tests

## Build, Test, and Development Commands
- `mvn clean compile`: clean and compile the project.
- `mvn test`: run test suite (requires valid DB/API config for many tests).
- `mvn clean package`: build runnable JAR in `target/`.
- `mvn spring-boot:run`: start local server on `http://localhost:8080`.

Use PowerShell env vars before running:
`$env:DEEPSEEK_API_KEY="..."` and `$env:EMBEDDING_API_KEY="..."`.

## Coding Style & Naming Conventions
- Follow standard Java conventions: 4-space indentation, UTF-8 source, one top-level class per file.
- Packages are lowercase (for example `com.claude.learn.service`).
- Class names use `PascalCase`; methods/fields use `camelCase`; constants use `UPPER_SNAKE_CASE`.
- Keep controller classes ending with `Controller`, service classes ending with `Service`, and test classes ending with `Test`.
- Prefer constructor injection and keep business logic in `service/` or `agent/`, not controllers.

## Testing Guidelines
- Framework: JUnit 5 via `spring-boot-starter-test`.
- Place tests under mirrored package paths in `src/test/java`.
- Name tests `*Test` (for example `HybridSearchTest`).
- Prefer focused tests by feature (ingest, retrieval, orchestration, tools). Add regression tests for bug fixes.
- No enforced coverage gate is configured; ensure changed paths are covered before opening a PR.

## Commit & Pull Request Guidelines
- History includes both Conventional Commits (`feat:`, `chore:`) and short numbered messages. Prefer Conventional Commit prefixes for new work.
- Keep commits small and scoped (one concern per commit).
- PRs should include:
  1. Problem and solution summary
  2. Config/schema changes (if any)
  3. Test evidence (`mvn test` results or targeted test notes)
  4. API examples or screenshots when UI/endpoint behavior changes

## Security & Configuration Tips
- Do not commit API keys, JWT secrets, or database passwords.
- Move hardcoded local values to `application.yml` or environment variables.
- Validate `schema.sql` changes against a local PostgreSQL instance with `pgvector` enabled.
