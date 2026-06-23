INSERT INTO sectors (sector_index, name, azimuth, site_id,
                     created_at, created_by, last_modified_at, last_modified_by)
SELECT t.sector_index,
       t.name,
       t.azimuth,
       (SELECT id FROM sites WHERE site_code = t.site_code),
       t.created_at,
       t.created_by,
       t.last_modified_at,
       t.last_modified_by
FROM dblink(
             'host=localhost port=5432 dbname=pulse_db user=pguser password=password',
             'SELECT s.sector_index, s.name, s.azimuth, s.created_at, s.created_by,
                     s.last_modified_at, s.last_modified_by, si.site_code
              FROM sectors s
                       JOIN sites si ON si.id = s.site_id'
     ) AS t(sector_index integer, name varchar, azimuth integer,
            created_at timestamp, created_by varchar,
            last_modified_at timestamp, last_modified_by varchar,
            site_code varchar)
ON CONFLICT (name) DO NOTHING;