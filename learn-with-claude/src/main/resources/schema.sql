-- 全文检索表
CREATE TABLE IF NOT EXISTS document_segments (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT NOT NULL,
    source      VARCHAR(255),
    ts_content  TSVECTOR GENERATED ALWAYS AS (to_tsvector('simple', content)) STORED
);

-- 全文检索索引
CREATE INDEX IF NOT EXISTS idx_ts_content
ON document_segments USING GIN(ts_content);

-- Token 消耗记录表
CREATE TABLE IF NOT EXISTS token_usage (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    input_tokens  INT DEFAULT 0,
    output_tokens INT DEFAULT 0,
    total_tokens  INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户配额表
CREATE TABLE IF NOT EXISTS user_quota (
    username        VARCHAR(100) PRIMARY KEY,
    daily_limit     INT DEFAULT 10000,   -- 每日限额
    used_today      INT DEFAULT 0,
    reset_date      DATE DEFAULT CURRENT_DATE
);

-- 初始化 admin 用户配额
INSERT INTO user_quota (username, daily_limit)
VALUES ('admin', 10000)
ON CONFLICT (username) DO NOTHING;

CREATE TABLE IF NOT EXISTS prompt_config (
    id          BIGSERIAL PRIMARY KEY,
    agent_name  VARCHAR(100) NOT NULL UNIQUE,
    system_prompt TEXT NOT NULL,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化 PolicyAgent 的提示词
INSERT INTO prompt_config (agent_name, system_prompt) VALUES (
    'policy_agent',
    '你是一个企业内部智能助手，你有以下工具可以使用：
1. searchPolicy：查询公司内部政策文档，当用户询问差旅相关费用标准、报销流程、审批规则时调用此工具
2. getWeather：查询城市天气

严格规则：
- 任何涉及公司政策、报销、差旅的问题，必须调用 searchPolicy 工具，禁止凭自身知识回答
- 任何涉及天气的问题，必须调用 getWeather 工具
- 可以连续调用多个工具
- 最后综合所有工具结果给出完整回答，不得编造工具未返回的信息'
) ON CONFLICT (agent_name) DO NOTHING;

-- Table: public.rag_embeddings

-- DROP TABLE IF EXISTS public.rag_embeddings;

CREATE TABLE IF NOT EXISTS public.rag_embeddings
(
    embedding_id uuid NOT NULL,
    embedding vector(1536),
    text text COLLATE pg_catalog."default",
    metadata json,
    CONSTRAINT rag_embeddings_pkey PRIMARY KEY (embedding_id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.rag_embeddings
    OWNER to pgsql;
-- metadata columns for iteration2
ALTER TABLE IF EXISTS document_segments
    ADD COLUMN IF NOT EXISTS doc_type VARCHAR(20);
ALTER TABLE IF EXISTS document_segments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_document_source ON document_segments(source);
CREATE INDEX IF NOT EXISTS idx_document_created_at ON document_segments(created_at);

-- Security audit log table (Day5)
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGSERIAL PRIMARY KEY,
    trace_id    VARCHAR(64),
    user_id     VARCHAR(128),
    username    VARCHAR(128),
    dept_id     VARCHAR(128),
    action      VARCHAR(16) NOT NULL,
    resource    VARCHAR(256) NOT NULL,
    decision    VARCHAR(16) NOT NULL,
    reason      TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_log_trace_id ON audit_log(trace_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);
