-- V31__create_cell_mappings.sql
-- Maps a previous (replaced) cell to the new cell that replaced it, so the previous
-- cell can be excluded from cell search/listing/counts while its historical data
-- (KPI/alarm/anomaly, all keyed by cell_name) remains untouched and queryable.

CREATE TABLE cell_mappings (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    previous_cell_id  BIGINT       NOT NULL UNIQUE REFERENCES cells(id),
    new_cell_id       BIGINT       NOT NULL UNIQUE REFERENCES cells(id),
    created_at        TIMESTAMP    NOT NULL,
    created_by        VARCHAR(255),
    last_modified_at  TIMESTAMP,
    last_modified_by  VARCHAR(255),
    CONSTRAINT chk_cell_mappings_distinct CHECK (previous_cell_id <> new_cell_id)
);

CREATE INDEX idx_cell_mappings_previous_cell_id ON cell_mappings(previous_cell_id);
CREATE INDEX idx_cell_mappings_new_cell_id ON cell_mappings(new_cell_id);

COMMENT ON TABLE cell_mappings IS 'Links a previous (replaced) cell to the new cell that replaced it. UNIQUE constraints on both FKs enforce a linked-list shape (no branching/merging), supporting chained replacements over time.';
