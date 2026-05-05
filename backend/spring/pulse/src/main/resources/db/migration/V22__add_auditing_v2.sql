-- WORST CELLS --
ALTER TABLE worst_cells
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE worst_cells
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE worst_cells
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN worst_cells.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN worst_cells.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN worst_cells.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN worst_cells.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- AREA --
ALTER TABLE areas
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;


UPDATE areas
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE areas
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN areas.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN areas.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN areas.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN areas.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- AREA DISTRICT-CODE MAPPING --
ALTER TABLE area_district_code_mapping
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE area_district_code_mapping
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE area_district_code_mapping
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN area_district_code_mapping.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN area_district_code_mapping.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN area_district_code_mapping.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN area_district_code_mapping.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- AREA TYPE --
ALTER TABLE area_types
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE area_types
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE area_types
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN area_types.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN area_types.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN area_types.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN area_types.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- BAND --
ALTER TABLE bands
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE bands
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE bands
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN bands.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN bands.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN bands.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN bands.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';

-- BASIC KPI --
ALTER TABLE basic_kpi
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE basic_kpi
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE basic_kpi
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN basic_kpi.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN basic_kpi.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN basic_kpi.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN basic_kpi.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- CARRIER --
ALTER TABLE carriers
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE carriers
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE carriers
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN carriers.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN carriers.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN carriers.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN carriers.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- GRANULARITY --
ALTER TABLE granularity
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE granularity
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE granularity
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN granularity.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN granularity.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN granularity.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN granularity.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- KPI MAPPING TO OSS --
ALTER TABLE kpi_mapping
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE kpi_mapping
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE kpi_mapping
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN kpi_mapping.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN kpi_mapping.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN kpi_mapping.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN kpi_mapping.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- OSS --
ALTER TABLE oss
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE oss
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE oss
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN oss.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN oss.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN oss.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN oss.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- RAT --
ALTER TABLE rat
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE rat
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE rat
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN rat.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN rat.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN rat.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN rat.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- STANDARD KPI --
ALTER TABLE standard_kpi
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE standard_kpi
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE standard_kpi
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN standard_kpi.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN standard_kpi.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN standard_kpi.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN standard_kpi.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- STANDARD RAW KPI MAPPING --
ALTER TABLE standard_raw_kpi_mapping
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE standard_raw_kpi_mapping
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE standard_raw_kpi_mapping
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN standard_raw_kpi_mapping.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN standard_raw_kpi_mapping.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN standard_raw_kpi_mapping.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN standard_raw_kpi_mapping.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';


-- USER TO AREA MAPPING --
ALTER TABLE user_area_mapping
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

UPDATE user_area_mapping
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM';

ALTER TABLE user_area_mapping
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN user_area_mapping.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN user_area_mapping.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN user_area_mapping.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN user_area_mapping.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';