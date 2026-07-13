-- Pass 1: backfill from most recent kpi_values row per cell
UPDATE cells c
SET district_code_id = kv.district_code_id
FROM (
         SELECT DISTINCT ON (cell_name) cell_name, district_code_id
         FROM kpi_values
         WHERE district_code_id IS NOT NULL
         ORDER BY cell_name, timestamp DESC
     ) kv
WHERE c.cell_name = kv.cell_name
  AND c.district_code_id IS NULL;

-- Pass 2: fallback for cells with no kpi_values rows — literal prefix match,
-- longest code wins (mirrors district_codes ORDER BY LENGTH(code) DESC in the Python script)
UPDATE cells c
SET district_code_id = matched.district_code_id
FROM (
         SELECT DISTINCT ON (c2.id) c2.id AS cell_id, dc.id AS district_code_id
         FROM cells c2
                  JOIN district_codes dc ON starts_with(c2.cell_name, dc.code)
         WHERE c2.district_code_id IS NULL
         ORDER BY c2.id, LENGTH(dc.code) DESC
     ) matched
WHERE c.id = matched.cell_id;