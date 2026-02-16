-- ENABLE COMPRESSION --
ALTER TABLE kpi_values_hour
SET (
  timescaledb.compress,
  timescaledb.compress_orderby = 'timestamp DESC',
  timescaledb.compress_segmentby =
    'cell_name, standard_kpi_id, rat_id, granularity_id, district_code_id'
);

-- COMPRESSION POLICY --
SELECT add_compression_policy(
  'kpi_values_hour',
  INTERVAL '7 days'
);

-- RETENTION POLICY --
SELECT add_retention_policy(
  'kpi_values_hour',
  INTERVAL '30 days'
);