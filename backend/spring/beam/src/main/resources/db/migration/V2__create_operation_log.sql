CREATE TABLE IF NOT EXISTS operation_log (
    id              BIGSERIAL       PRIMARY KEY,

    -- Which entity was touched
    entity_name     VARCHAR(100)    NOT NULL,   -- e.g. 'Cell', 'Site'
    entity_id       VARCHAR(100)    NOT NULL,   -- PK of the affected row (as string)

    -- What happened
    operation       VARCHAR(10)     NOT NULL,   -- CREATE | UPDATE | DELETE
    performed_at    TIMESTAMP       NOT NULL,
    performed_by    VARCHAR(255)    NOT NULL,   -- JWT 'sub' claim
    changes         JSONB           NOT NULL DEFAULT '{}'
);

-- Fast admin queries: "show me all changes to Cell rows"
CREATE INDEX idx_oplog_entity        ON operation_log (entity_name, entity_id);

-- Fast admin queries: "show me everything done by a specific user"
CREATE INDEX idx_oplog_performed_by  ON operation_log (performed_by);

-- Fast admin queries: "show me changes in a time window"
CREATE INDEX idx_oplog_performed_at  ON operation_log (performed_at DESC);

-- JSONB GIN index: allows querying changes->'fieldName' efficiently
CREATE INDEX idx_oplog_changes       ON operation_log USING GIN (changes);

COMMENT ON TABLE  operation_log                IS 'Immutable audit trail for all @AuditLog-annotated entities.';
COMMENT ON COLUMN operation_log.changes        IS 'JSONB field diff: {field: {old, new}}. old/new absent for CREATE/DELETE respectively.';
COMMENT ON COLUMN operation_log.entity_id      IS 'String representation of the entity primary key at time of event.';