-- alarm_sources

CREATE TABLE alarm_sources
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    label            VARCHAR(255) NOT NULL,

    -- AuditEntity columns
    created_at       TIMESTAMP    NOT NULL,
    last_modified_at TIMESTAMP,
    created_by       VARCHAR(100) NOT NULL,
    last_modified_by VARCHAR(100),

    CONSTRAINT uq_alarm_sources_name UNIQUE (name),
    CONSTRAINT uq_alarm_sources_label UNIQUE (label)
);


-- alarm_types

CREATE TABLE alarm_types
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    CONSTRAINT uq_alarm_types_name UNIQUE (name)
);

-- alarm_definitions

CREATE TABLE alarm_definitions
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alarm_source_id BIGINT       NOT NULL,
    alarm_code      BIGINT       NOT NULL,
    alarm_name      VARCHAR(500) NOT NULL,
    description     TEXT,

    CONSTRAINT fk_alarm_definitions_alarm_source
        FOREIGN KEY (alarm_source_id) REFERENCES alarm_sources (id),

    CONSTRAINT uq_alarm_definitions_source_code
        UNIQUE (alarm_source_id, alarm_code)
);

CREATE INDEX idx_alarm_definitions_alarm_source_id
    ON alarm_definitions (alarm_source_id);

-- alarms
-- Step 1: Create table WITHOUT PRIMARY KEY constraint
CREATE TABLE alarms
(
    id                  BIGSERIAL    NOT NULL,
    node_name           VARCHAR(255) NOT NULL,
    alarm_definition_id BIGINT       NOT NULL,
    occurrence_time     TIMESTAMP    NOT NULL,
    specific_problem    VARCHAR(1000),
    severity            VARCHAR(20)  NOT NULL,
    raw_severity        VARCHAR(100),
    ack_state           VARCHAR(20)  NOT NULL,
    alarm_id            BIGINT       NOT NULL,
    alarm_type_id       BIGINT       NOT NULL,
    location            VARCHAR(500),
    additional_info     TEXT,
    description         TEXT,
    clear_state         VARCHAR(20)  NOT NULL,
    clear_time          TIMESTAMP,
    alarm_source_id     BIGINT       NOT NULL
);

-- Step 2: Convert to hypertable
SELECT create_hypertable('alarms', 'occurrence_time', chunk_time_interval => INTERVAL '1 day', if_not_exists => TRUE);

-- Step 3: Unique index on id (acts as PRIMARY KEY for queries/JPA @Id)
CREATE UNIQUE INDEX uq_alarms_id ON alarms (occurrence_time, id);

-- Step 4: Business-logic dedup constraint
-- All columns here are NOT NULL, so no COALESCE / sentinel handling needed.
-- Because severity/ack_state/clear_state are part of the key, a state
-- change (e.g. clearing) produces a NEW row rather than colliding with
-- the old one — this is intentional (append-only lifecycle rows).
CREATE UNIQUE INDEX uq_alarms_dedup ON alarms (
                                               occurrence_time,
                                               node_name,
                                               alarm_definition_id,
                                               severity,
                                               ack_state,
                                               clear_state,
                                               alarm_id,
                                               COALESCE(location, ''),
                                               alarm_source_id
    );

-- Step 5: Foreign keys
ALTER TABLE alarms
    ADD CONSTRAINT fk_alarms_alarm_definition FOREIGN KEY (alarm_definition_id) REFERENCES alarm_definitions (id);

ALTER TABLE alarms
    ADD CONSTRAINT fk_alarms_alarm_type FOREIGN KEY (alarm_type_id) REFERENCES alarm_types (id);

ALTER TABLE alarms
    ADD CONSTRAINT fk_alarms_alarm_source FOREIGN KEY (alarm_source_id) REFERENCES alarm_sources (id);

-- Step 6: Check constraints for enum columns
ALTER TABLE alarms
    ADD CONSTRAINT chk_alarms_severity
        CHECK (severity IN ('CRITICAL', 'MAJOR', 'MINOR', 'WARNING', 'INDETERMINATE', 'CLEARED'));

ALTER TABLE alarms
    ADD CONSTRAINT chk_alarms_ack_state
        CHECK (ack_state IN ('ACKNOWLEDGED', 'UNACKNOWLEDGED', 'UNKNOWN'));

ALTER TABLE alarms
    ADD CONSTRAINT chk_alarms_clear_state
        CHECK (clear_state IN ('CLEARED', 'UNCLEARED', 'UNKNOWN'));

-- Step 7: Indexes for query performance
CREATE INDEX idx_alarms_node_name
    ON alarms (occurrence_time DESC, node_name);

CREATE INDEX idx_alarms_alarm_source_id
    ON alarms (occurrence_time DESC, alarm_source_id);

CREATE INDEX idx_alarms_alarm_type_id
    ON alarms (occurrence_time DESC, alarm_type_id);

CREATE INDEX idx_alarms_alarm_definition_id
    ON alarms (occurrence_time DESC, alarm_definition_id);

CREATE INDEX idx_alarms_severity
    ON alarms (occurrence_time DESC, severity);

CREATE INDEX idx_alarms_clear_state
    ON alarms (occurrence_time DESC, clear_state);

-- Composite index: node-level filtering within a time range
CREATE INDEX idx_alarms_node_source_time
    ON alarms (occurrence_time DESC, node_name, alarm_source_id);

-- ============================================================
-- Compression
-- Safe to compress aggressively since rows are never updated —
-- clearing inserts a new row rather than mutating an existing one.
-- ============================================================
ALTER TABLE alarms
    SET (
        timescaledb.compress,
        timescaledb.compress_segmentby = 'alarm_source_id, alarm_type_id',
        timescaledb.compress_orderby = 'occurrence_time DESC'
        );

SELECT add_compression_policy('alarms', INTERVAL '3 days');

-- ============================================================
-- Retention: drop chunks entirely older than 6 months
-- ============================================================
SELECT add_retention_policy('alarms', INTERVAL '6 months');