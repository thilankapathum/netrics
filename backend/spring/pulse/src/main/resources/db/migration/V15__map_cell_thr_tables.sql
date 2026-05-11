-- MAP CELL THRESHOLD SET
CREATE TABLE map_cell_thr_sets (
    id BIGSERIAL PRIMARY KEY,

    standard_kpi_id BIGINT NOT NULL,
    granularity_id BIGINT NOT NULL,
    rat_id BIGINT NOT NULL,

    user_id VARCHAR(100) NOT NULL,

    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    last_modified_by VARCHAR(100),

    CONSTRAINT fk_thrset_kpi FOREIGN KEY (standard_kpi_id)
        REFERENCES standard_kpi(id),

    CONSTRAINT fk_thrset_granularity FOREIGN KEY (granularity_id)
        REFERENCES granularity(id),

    CONSTRAINT fk_thrset_rat FOREIGN KEY (rat_id)
        REFERENCES rat(id)
);

-- UNIQUE CONSTRAINTS

-- 1. General uniqueness
CREATE UNIQUE INDEX uq_thrset_user_admin_combo
    ON map_cell_thr_sets (standard_kpi_id, granularity_id, rat_id, user_id, is_admin)
    WHERE is_deleted = FALSE;

-- 2. Only ONE admin set globally per KPI+granularity+rat
CREATE UNIQUE INDEX uq_thrset_single_admin
    ON map_cell_thr_sets (standard_kpi_id, granularity_id, rat_id)
    WHERE is_admin = TRUE AND is_deleted = FALSE;


-- INDEXES (performance)

CREATE INDEX idx_thrset_lookup
    ON map_cell_thr_sets (standard_kpi_id, granularity_id, rat_id);

CREATE INDEX idx_thrset_user
    ON map_cell_thr_sets (user_id);

CREATE INDEX idx_thrset_main_fetch
    ON map_cell_thr_sets (standard_kpi_id, granularity_id, rat_id, user_id, is_admin);

-- =========================

-- MAP CELL THRESHOLDS
CREATE TABLE map_cell_thresholds (
    id BIGSERIAL PRIMARY KEY,

    map_cell_thr_set_id BIGINT NOT NULL,

    min_value DOUBLE PRECISION NOT NULL,
    max_value DOUBLE PRECISION NOT NULL,

    color VARCHAR(50),
    label VARCHAR(100),

    priority INT NOT NULL,

    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,
    last_modified_by VARCHAR(100),

    CONSTRAINT fk_threshold_set FOREIGN KEY (map_cell_thr_set_id)
        REFERENCES map_cell_thr_sets(id)
        ON DELETE CASCADE
);

-- UNIQUE CONSTRAINTS

CREATE UNIQUE INDEX uq_threshold_priority
    ON map_cell_thresholds (map_cell_thr_set_id, priority);

-- INDEXES

CREATE INDEX idx_threshold_set
    ON map_cell_thresholds (map_cell_thr_set_id);