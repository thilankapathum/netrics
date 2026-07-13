ALTER TABLE cells
    ADD COLUMN district_code_id BIGINT;

ALTER TABLE cells
    ADD CONSTRAINT fk_cells_district_code
        FOREIGN KEY (district_code_id)
            REFERENCES district_codes(id);

CREATE INDEX idx_cells_district_code_id
    ON cells(district_code_id);