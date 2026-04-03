CREATE TABLE sectors (
    id BIGSERIAL PRIMARY KEY,
    sector_index INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    azimuth INTEGER,
    site_id BIGINT NOT NULL,

    -- Foreign Key
    CONSTRAINT fk_sectors_site
        FOREIGN KEY (site_id)
            REFERENCES sites (id),

    -- Unique constraints
    CONSTRAINT uq_sectors_site_sector_index
        UNIQUE (site_id, sector_index),

    CONSTRAINT uq_sectors_name
        UNIQUE (name),

    -- Validations
    CONSTRAINT chk_sectors_index_min
        CHECK (sector_index >= 1),

    CONSTRAINT chk_sectors_azimuth_range
        CHECK (azimuth >= 0 AND azimuth <= 360)
);