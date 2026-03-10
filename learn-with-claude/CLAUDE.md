# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java 21 Spring Boot application implementing an enterprise knowledge assistant with RAG (Retrieval-Augmented Generation). It uses LangChain4j for LLM orchestration, PostgreSQL with pgvector for vector storage, and implements a multi-agent architecture for handling complex queries.

**Key capabilities**: JWT authentication, document upload/chunking (PDF/DOCX), hybrid search (vector + keyword), agent tool calling, SSE streaming chat, token usage tracking, circuit breaker pattern, and distributed tracing.

## Build and Development Commands

```bash
# Compile the project
mvn clean compile

# Run tests (requires valid DB and API keys)
mvn test

# Build runnable JAR
mvn clean package

# Start local server on http://localhost:8080
mvn spring-boot:run
```

**Environment setup**: Before running, set these environment variables:
- `DEEPSEEK_API_KEY` - Primary chat model API key
- `EMBEDDING_API_KEY` - Embedding model API key (DashScope compatible)
- `ANTHROPIC_API_KEY` - Backup model API key (optional)
- `SPRING_DATASOURCE_URL` - PostgreSQL connection (default: jdbc:postgresql://localhost:5432/ragdb)
- `SPRING_DATASOURCE_USERNAME` - Database username (default: postgres)
- `SPRING_DATASOURCE_PASSWORD` - Database password (default: postgres)

PowerShell example:
```powershell
$env:DEEPSEEK_API_KEY="your_key"
$env:EMBEDDING_API_KEY="your_key"
```

**Database requirement**: PostgreSQL 14+ with pgvector extension installed:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## Multi-Agent Architecture

The system implements a sophisticated multi-agent pattern with specialized roles:

### Agent Hierarchy

1. **OrchestratorAgent** (`agent/OrchestratorAgent.java`) - Top-level coordinator that:
   - Routes user queries to appropriate specialized agents
   - Manages the agent execution flow and step tracking
   - Handles tool selection and execution via `AgentTools`
   - Implements retry logic and error handling

2. **PolicyAgent** (`agent/PolicyAgent.java`) - Specialized for policy/document retrieval:
   - Uses hybrid search (vector + keyword) via `HybridSearchService`
   - Calls `searchPolicy` tool to retrieve relevant document chunks
   - Formats results with source citations

3. **SummaryAgent** (`agent/SummaryAgent.java`) - Generates final responses:
   - Synthesizes information from tool results
   - Produces user-facing answers with proper formatting

4. **HumanInLoopAgent** (`agent/HumanInLoopAgent.java`) - Handles human intervention:
   - Pauses execution for user confirmation when needed
   - Implements approval workflows for sensitive operations

### Agent Runtime System

The `agent/runtime/` package provides execution infrastructure:

- **AgentRun**: Tracks entire agent execution lifecycle with status (RUNNING, COMPLETED, FAILED, TIMEOUT)
- **AgentStep**: Represents individual steps within an agent run, including tool calls and results
- **AgentRunStatus/AgentStepStatus**: Enums for tracking execution state
- **AgentErrorCode**: Standardized error codes for debugging

Key configuration in `application.yml`:
```yaml
agent:
  runtime:
    max-steps: 3           # Maximum steps per agent run
    step-timeout-ms: 15000 # Timeout per step
    retry-times: 1         # Retry attempts on failure
```

### Model Abstraction Layer

The system supports multiple LLM providers through a flexible abstraction (`agent/invoker/`):

- **ModelClientFactory**: Creates appropriate client based on provider type
- **LanguageModelClient**: Interface for model invocation
- **LangChain4jModelClient**: Implementation using LangChain4j
- **ModelInvoker**: High-level API for model calls with streaming support

**Provider configuration** in `application.yml`:
```yaml
app:
  models:
    primary:    # Main model (e.g., DeepSeek)
      provider: openai-compatible
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      model-name: deepseek-chat
    fallback:   # Local fallback (e.g., Ollama)
      provider: openai-compatible
      base-url: http://localhost:11434/v1
      model-name: llama3.2:3b
    back:       # Backup model (e.g., Claude)
      provider: anthropic
      api-key: ${ANTHROPIC_API_KEY}
      model-name: claude-sonnet-4-6
```

Supported providers: `openai-compatible`, `anthropic`, `ollama`

### Tool Execution Framework

Tools are wrapped with monitoring and retry capabilities (`agent/tool/`):

- **ToolExecutionWrapper**: Wraps tool calls with error handling and metrics
- **ToolExecutionMonitor**: Tracks execution stats (success/failure counts, latency)
- **ToolRetryStrategy**: Implements retry logic for transient failures
- **RetryableErrorType**: Classifies errors (RATE_LIMIT, TIMEOUT, NETWORK, etc.)
- **ToolExecutionResult**: Standardized result format with success/error info

Tools are registered in `AgentTools.java` and include:
- `searchPolicy`: Retrieves relevant policy documents
- `getWeather`: Example external API tool

### Circuit Breaker Pattern

The system uses Resilience4j for fault tolerance:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      deepseek:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
```

Applied to model invocations to prevent cascading failures when external APIs are down.

## Key Architectural Patterns

1. **Hybrid Search**: Combines vector similarity (pgvector) with keyword search (PostgreSQL full-text) for better retrieval accuracy. Implementation in `service/HybridSearchService.java`.

2. **Document Chunking**: Uses LangChain4j's recursive splitter with configurable chunk size (300 tokens) and overlap (30 tokens). Configuration in `application.yml` under `rag.chunk`.

3. **Token Monitoring**: Tracks token usage per user with daily quotas. Implementation in `service/TokenMonitorService.java` and `domain/TokenUsage.java`.

4. **SSE Streaming**: Supports Server-Sent Events for real-time chat responses. Endpoint: `GET /api/chat/stream?message=xxx&token=xxx`.

5. **JWT Authentication**: Custom filter (`filter/JwtAuthenticationFilter.java`) validates tokens on all protected endpoints. Demo credentials: admin/123456.

## Testing Approach

Tests are in `src/test/java/com/claude/learn/` and require:
- Running PostgreSQL with pgvector
- Valid API keys in environment
- Accessible model endpoints

Test categories:
- Integration tests for end-to-end flows (document upload → retrieval → chat)
- Service tests for individual components (HybridSearchService, TokenMonitorService)
- Agent tests for orchestration logic

Run specific test class:
```bash
mvn test -Dtest=HybridSearchTest
```

## Important Notes

1. **Hardcoded Configuration**: `LangChainConfig.java` contains hardcoded database URL (192.168.20.129:5432/ragdb) for EmbeddingStore. Update this if your environment differs.

2. **Demo Authentication**: Current login is hardcoded in `AuthController.java`. Not suitable for production.

3. **Document Parsers**: Only PDF and DOCX are supported via LangChain4j parsers (Apache PDFBox and POI).

4. **Model Selection**: The system automatically falls back to secondary models if primary fails. Check circuit breaker metrics at `/actuator/health`.

## Additional Resources

- See `AGENTS.md` for detailed coding guidelines, naming conventions, commit standards, and security practices
- See `README.md` (Chinese) for API endpoint documentation and usage examples
- See `docs/` directory for interview preparation and optimization notes
