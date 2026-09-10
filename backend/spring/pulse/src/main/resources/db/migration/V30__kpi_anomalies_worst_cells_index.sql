-- Covering index for the anomaly-cells "worst cells" query path (KpiAnomalyRepository:
-- findAllAnomalyCellsByArea / findAllAnomalyCellsByAreaPaged / countAnomalyCellsByArea /
-- countAnomaliesByKpiAndSeverity), which all filter kpi_anomalies by
-- (rat_id, granularity_id, timestamp range) plus optional standard_kpi_id/severity/
-- has_alarm_correlation. The existing idx_kpi_anomalies_lookup covers
-- (rat_id, granularity_id, timestamp, severity) but not standard_kpi_id, and none of the
-- existing indexes support all four filters together.
--
-- kpi_anomalies is a TimescaleDB hypertable, which does not support CREATE INDEX CONCURRENTLY
-- (errors with "hypertables do not support concurrent index creation"), so this is a plain
-- CREATE INDEX — it takes a brief write lock per chunk while building, same as any other
-- index on this table (see idx_kpi_anomalies_lookup et al. in V24/V29).
CREATE INDEX IF NOT EXISTS idx_kpi_anomalies_worst_cells
    ON kpi_anomalies (rat_id, granularity_id, timestamp DESC, standard_kpi_id, severity)
    INCLUDE (cell_name, has_alarm_correlation, observed_value, robust_z_score);
