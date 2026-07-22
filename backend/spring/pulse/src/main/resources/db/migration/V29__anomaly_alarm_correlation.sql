ALTER TABLE granularity
    ADD COLUMN IF NOT EXISTS window_seconds INT;

-- 1. New column on alarms: populated at ingestion time (Python) by parsing
-- `location` / `additional_info` string for "%Cell Name".
ALTER TABLE alarms
    ADD COLUMN IF NOT EXISTS parsed_cell_name VARCHAR(255);

CREATE INDEX IF NOT EXISTS idx_alarms_parsed_cell_name
    ON alarms (parsed_cell_name)
    WHERE parsed_cell_name IS NOT NULL;

-- 2. Correlation table — hypertable, partitioned on anomaly_timestamp so it
--    can carry its own retention policy aligned to kpi_anomalies (90 days).
--    No FK constraints to alarms/kpi_anomalies: both are hypertables whose
--    unique indexes include the partition column, so a plain FK on id alone
--    isn't possible — and would block chunk-drop retention anyway.
CREATE TABLE anomaly_alarm_correlations
(
    id                  BIGSERIAL   NOT NULL,
    anomaly_id          BIGINT      NOT NULL,
    anomaly_timestamp   TIMESTAMP   NOT NULL, -- copied from kpi_anomalies.timestamp; used as partition key
    alarm_id            BIGINT      NOT NULL,
    alarm_definition_id BIGINT      NOT NULL, -- FK'd conceptually to alarm_definitions; grouping/display key
    alarm_source_id     BIGINT      NOT NULL, -- denormalized for cheap vendor filtering
    match_level         VARCHAR(10) NOT NULL, -- 'NODE' | 'CELL'
    overlap_seconds     INTEGER     NOT NULL,
    window_seconds      INTEGER     NOT NULL,
    created_at          TIMESTAMP   NOT NULL DEFAULT now()
);

SELECT create_hypertable('anomaly_alarm_correlations', 'anomaly_timestamp',
                         chunk_time_interval => INTERVAL '1 month', if_not_exists => TRUE);

CREATE UNIQUE INDEX uq_correlations_dedup
    ON anomaly_alarm_correlations (anomaly_timestamp, anomaly_id, alarm_id);

CREATE INDEX idx_correlations_anomaly_id
    ON anomaly_alarm_correlations (anomaly_timestamp DESC, anomaly_id);

CREATE INDEX idx_correlations_alarm_definition_id
    ON anomaly_alarm_correlations (anomaly_timestamp DESC, alarm_definition_id);

SELECT add_retention_policy('anomaly_alarm_correlations', INTERVAL '90 days');

-- 3. Rollup columns on kpi_anomalies, for cheap filter/sort on the anomaly
--    list page without joining anomaly_alarm_correlations per page load.
ALTER TABLE kpi_anomalies
    ADD COLUMN IF NOT EXISTS has_alarm_correlation    BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS distinct_alarm_def_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_alarm_occurrences  INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS best_match_level         VARCHAR(10);

CREATE INDEX IF NOT EXISTS idx_kpi_anomalies_has_alarm_correlation
    ON kpi_anomalies (timestamp DESC, has_alarm_correlation);