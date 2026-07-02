CREATE INDEX IF NOT EXISTS idx_kpi_values_hour_trend
    ON kpi_values_hour (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp)
    INCLUDE (kpi_value, numerator_kpi_value, denominator_kpi_value);

ALTER TABLE kpi_values SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'standard_kpi_id, rat_id, granularity_id, district_code_id, cell_name',
    timescaledb.compress_orderby   = 'timestamp DESC'
    );

CREATE INDEX IF NOT EXISTS idx_kpi_values_history_lookup
    ON kpi_values (cell_name, standard_kpi_id, rat_id, granularity_id, timestamp DESC)
    INCLUDE (kpi_value, numerator_kpi_value, denominator_kpi_value);