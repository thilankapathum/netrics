-- Run against beam_db
-- Copy all sites from pulse_db to beam_db
CREATE EXTENSION IF NOT EXISTS dblink;

INSERT INTO sites (site_code, site_name, latitude, longitude,
                   created_at, last_modified_at, created_by, last_modified_by)
SELECT site_code,
       site_name,
       latitude,
       longitude,
       created_at,
       last_modified_at,
       created_by,
       last_modified_by
FROM dblink(
             'host=localhost port=5432 dbname=pulse_db user=pguser password=password',
             'SELECT site_code, site_name, latitude, longitude, created_at, last_modified_at, created_by, last_modified_by FROM sites'
     ) AS t(site_code varchar, site_name varchar, latitude float8,
            longitude float8, created_at timestamp, last_modified_at timestamp,created_by varchar, last_modified_by varchar)
ON CONFLICT (site_code) DO NOTHING;