CREATE EXTENSION IF NOT EXISTS postgis;

-- Correct latitude & longitude column types from DECIMAL(9,6) to DOUBLE PRECISION
ALTER TABLE sites
    ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::DOUBLE PRECISION,
    ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::DOUBLE PRECISION;

-- Add generated geometry column (auto-updates when lat/lng change)
ALTER TABLE sites
    ADD COLUMN geom GEOGRAPHY(Point, 4326)
        GENERATED ALWAYS AS (
            ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
            ) STORED;

-- Add spatial index for efficient geo queries
CREATE INDEX idx_sites_geom
    ON sites
        USING GIST (geom);