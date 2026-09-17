-- V32__create_assistant_query_log.sql
-- Audit trail for the natural-language assistant: one row per question, recording how the
-- LLM's tool call was resolved. Also serves as the eval dataset for tuning prompts/aliases.

CREATE TABLE assistant_query_log (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     VARCHAR(64)  NOT NULL,
    question    VARCHAR(500) NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    tool        VARCHAR(50),
    params      JSONB,
    row_count   INT,
    latency_ms  INT          NOT NULL,
    error       VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_assistant_query_log_user_created ON assistant_query_log (user_id, created_at DESC);
