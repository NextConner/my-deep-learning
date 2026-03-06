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