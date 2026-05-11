CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE map_cell_thresholds
    ADD CONSTRAINT no_overlap_ranges
        EXCLUDE USING gist (
        map_cell_thr_set_id WITH =,
        numrange(min_value::numeric, max_value::numeric, '[)') WITH &&
        );

-- [: inclusive, ): exclusive