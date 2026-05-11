DROP INDEX IF EXISTS idx_kpi_values_hour_main;
DROP INDEX IF EXISTS idx_kpi_values_hour_cell_standard_rat_gran_time;
DROP INDEX IF EXISTS idx_kpi_values_hour_covering;

CREATE INDEX idx_kpi_values_hour_query
ON kpi_values_hour (timestamp DESC, cell_name, standard_kpi_id, rat_id, granularity_id)
INCLUDE (kpi_value, numerator_kpi_value, denominator_kpi_value);