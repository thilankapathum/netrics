CREATE TABLE kpi_anomalies
(
    id              BIGSERIAL PRIMARY KEY,
    cell_name       VARCHAR(255)     NOT NULL,
    standard_kpi_id BIGINT           NOT NULL,
    rat_id          BIGINT           NOT NULL,
    granularity_id  BIGINT           NOT NULL,
    timestamp       TIMESTAMP        NOT NULL,
    observed_value  DOUBLE PRECISION NOT NULL,
    baseline_median DOUBLE PRECISION NOT NULL,
    mad             DOUBLE PRECISION NOT NULL,
    robust_z_score  DOUBLE PRECISION NOT NULL,
    severity        VARCHAR(20)      NOT NULL,
    detected_at     TIMESTAMP        NOT NULL DEFAULT NOW()
);

ALTER TABLE kpi_anomalies
    ADD CONSTRAINT fk_anomalies_standard_kpi FOREIGN KEY (standard_kpi_id) REFERENCES standard_kpi (id);

ALTER TABLE kpi_anomalies
    ADD CONSTRAINT fk_anomalies_rat FOREIGN KEY (rat_id) REFERENCES rat (id);

ALTER TABLE kpi_anomalies
    ADD CONSTRAINT fk_anomalies_granularity FOREIGN KEY (granularity_id) REFERENCES granularity (id);

CREATE UNIQUE INDEX uq_kpi_anomalies
    ON kpi_anomalies (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp);

CREATE INDEX idx_kpi_anomalies_lookup
    ON kpi_anomalies (rat_id, granularity_id, timestamp DESC, severity);