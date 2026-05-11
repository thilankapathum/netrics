-- Add latitude & longitude
ALTER TABLE sites
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION;

-- Add generated geom column
ALTER TABLE sites
    ADD COLUMN geom GEOGRAPHY(Point, 4326)
        GENERATED ALWAYS AS (
            ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)
            ) STORED;

-- Add spatial index
CREATE INDEX idx_sites_geom
    ON sites
        USING GIST (geom);