-- Get daily average of 'volume_kbyte_dl' for last 30 days
SELECT timestamp, SUM(kpi_value) kpi_value, SUM(numerator_kpi_value) numerator_kpi_value, SUM(denominator_kpi_value) denominator_kpi_value
FROM lte_fdd_kpi_day
WHERE lte_fdd_standard_kpi_id = (SELECT id FROM lte_fdd_standard_kpi WHERE kpi_name = 'volume_kbyte_dl')
AND timestamp BETWEEN DATE_SUB((SELECT DISTINCT timestamp FROM lte_fdd_kpi_day ORDER BY timestamp DESC LIMIT 1), INTERVAL 1 DAY)
AND (SELECT DISTINCT timestamp FROM lte_fdd_kpi_day ORDER BY timestamp DESC LIMIT 1)
GROUP BY timestamp;