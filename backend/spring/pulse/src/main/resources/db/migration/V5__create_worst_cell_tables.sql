CREATE TABLE worst_cells (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    cell_name VARCHAR(255) NOT NULL,
    value DOUBLE PRECISION,
    previous_value DOUBLE PRECISION,
    difference DOUBLE PRECISION,
    improved BOOLEAN,
    period VARCHAR(255) NOT NULL,
    exclude_zeroes BOOLEAN NOT NULL,
    area_id BIGINT NOT NULL,
    rat_id BIGINT NOT NULL,
    standard_kpi_id BIGINT NOT NULL,
    granularity_id BIGINT NOT NULL,

    CONSTRAINT uq_worst_cells UNIQUE (
        timestamp,
        period,
        cell_name,
        standard_kpi_id,
        rat_id,
        area_id,
        exclude_zeroes,
        granularity_id
    ),

    CONSTRAINT fk_worst_cells_area FOREIGN KEY (area_id)
        REFERENCES areas(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_worst_cells_rat FOREIGN KEY (rat_id)
        REFERENCES rat(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_worst_cells_standard_kpi FOREIGN KEY (standard_kpi_id)
        REFERENCES standard_kpi(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_worst_cells_granularity FOREIGN KEY (granularity_id)
        REFERENCES granularity(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_worst_cell_timestamp ON worst_cells(timestamp);
CREATE INDEX idx_worst_cell_cell_name ON worst_cells(cell_name);
CREATE INDEX idx_worst_cell_area ON worst_cells(area_id);
CREATE INDEX idx_worst_cell_rat ON worst_cells(rat_id);
CREATE INDEX idx_worst_cell_kpi ON worst_cells(standard_kpi_id);
CREATE INDEX idx_exclude_zeroes ON worst_cells(exclude_zeroes);
CREATE INDEX idx_granularity ON worst_cells(granularity_id);


-- WORST CELL COMMENTS --
CREATE TABLE worst_cell_comments (
    id BIGSERIAL PRIMARY KEY,
    comment TEXT,
    worst_cell_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    last_modified_at TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    last_modified_by VARCHAR(255)
);

ALTER TABLE worst_cell_comments
ADD CONSTRAINT fk_worst_cell_comments_worst_cell
FOREIGN KEY (worst_cell_id) REFERENCES worst_cells(id);

CREATE INDEX idx_worst_cell_comments_worst_cell
ON worst_cell_comments (worst_cell_id);

CREATE INDEX idx_worst_cell_comments_created_at
ON worst_cell_comments (created_at);
