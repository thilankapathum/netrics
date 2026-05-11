-- Add new columns
ALTER TABLE cells
    ADD COLUMN azimuth INTEGER,
    ADD COLUMN beamwidth INTEGER,
    ADD COLUMN is_multi_beam BOOLEAN DEFAULT FALSE;

-- Add new foreign key columns
ALTER TABLE cells
    ADD COLUMN carrier_id BIGINT,
    ADD COLUMN sector_id BIGINT;

-- Add foreign key constraints
ALTER TABLE cells
    ADD CONSTRAINT fk_cells_carrier
        FOREIGN KEY (carrier_id)
            REFERENCES carriers (id);

ALTER TABLE cells
    ADD CONSTRAINT fk_cells_sector
        FOREIGN KEY (sector_id)
            REFERENCES sectors (id);

-- Indexes
CREATE INDEX idx_cells_carrier_id
    ON cells(carrier_id);

CREATE INDEX idx_cells_sector_id
    ON cells(sector_id);

-- Validation
ALTER TABLE cells
    ADD CONSTRAINT chk_cells_azimuth_range
        CHECK (azimuth >= 0 AND azimuth <= 360);

ALTER TABLE cells
    ADD CONSTRAINT chk_cells_beamwidth_range
        CHECK (beamwidth >= 0 AND beamwidth <= 360);