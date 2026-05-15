INSERT INTO bands (id, name, number, unit,
                   created_at, created_by, last_modified_at, last_modified_by)
SELECT t.id,
       t.name,
       t.number,
       t.unit,
       t.created_at,
       t.created_by,
       t.last_modified_at,
       t.last_modified_by
FROM dblink(
             'host=localhost port=5432 dbname=pulse_db user=pguser password=password',
             'SELECT id, name, number, unit, created_at, created_by, last_modified_at, last_modified_by
              FROM bands'
     ) AS t(id bigint, name varchar, number integer, unit varchar,
            created_at timestamp, created_by varchar,
            last_modified_at timestamp, last_modified_by varchar)
ON CONFLICT (name) DO NOTHING;