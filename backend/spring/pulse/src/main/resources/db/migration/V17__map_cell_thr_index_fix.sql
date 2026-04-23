DROP INDEX IF EXISTS uq_thrset_user_admin_combo;

CREATE UNIQUE INDEX uq_thrset_admin_only
    ON map_cell_thr_sets (standard_kpi_id, granularity_id, rat_id)
    WHERE is_admin = TRUE
        AND is_deleted = FALSE;


CREATE UNIQUE INDEX uq_thrset_user_only
    ON map_cell_thr_sets (standard_kpi_id, granularity_id, rat_id, user_id)
    WHERE is_admin = FALSE
        AND is_deleted = FALSE;