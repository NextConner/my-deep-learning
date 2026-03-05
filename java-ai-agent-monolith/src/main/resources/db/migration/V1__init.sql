CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS tenant (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS app_user (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenant(id),
  username TEXT NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS document (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenant(id),
  title TEXT NOT NULL,
  source TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS document_chunk (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenant(id),
  document_id BIGINT NOT NULL REFERENCES document(id),
  chunk_index INT NOT NULL,
  content TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, document_id, chunk_index)
);

-- NOTE: embedding dimension depends on the embedding model you choose.
-- Keep it configurable later; for skeleton we use 1536 (common OpenAI embedding size).
CREATE TABLE IF NOT EXISTS chunk_embedding (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenant(id),
  document_id BIGINT NOT NULL REFERENCES document(id),
  chunk_id BIGINT NOT NULL REFERENCES document_chunk(id),
  embedding vector(1536) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (tenant_id, chunk_id)
);

CREATE INDEX IF NOT EXISTS idx_chunk_embedding_tenant ON chunk_embedding(tenant_id);
-- For real workloads consider HNSW/IVFFlat indexes. This is a minimal placeholder.

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGSERIAL PRIMARY KEY,
  tenant_id BIGINT NOT NULL REFERENCES tenant(id),
  user_id BIGINT REFERENCES app_user(id),
  action TEXT NOT NULL,
  provider TEXT,
  model TEXT,
  prompt_tokens INT,
  completion_tokens INT,
  latency_ms INT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

