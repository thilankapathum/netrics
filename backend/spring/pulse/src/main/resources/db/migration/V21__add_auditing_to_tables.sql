-- Add audit columns to 'sectors' table to support AuditEntity extension.
-- Existing rows are backfilled with sentinel values since historical data is unavailable.

ALTER TABLE sectors
    ADD COLUMN IF NOT EXISTS created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS last_modified_at TIMESTAMP   NULL,
    ADD COLUMN IF NOT EXISTS created_by      VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(255) NULL;

-- Backfill: mark all pre-existing rows as created by SYSTEM at current time.
-- This satisfies the NOT NULL constraint while being honest that the true
UPDATE sectors
SET created_at = NOW(),
    created_by = 'SYSTEM'
WHERE created_by = 'SYSTEM'; -- no-op guard; runs cleanly on a fresh backfill

-- Remove the DEFAULT so that the application (via AuditingEntityListener)
-- is solely responsible for populating these fields going forward.
ALTER TABLE sectors
    ALTER COLUMN created_at   DROP DEFAULT,
    ALTER COLUMN created_by   DROP DEFAULT;

COMMENT ON COLUMN sectors.created_at        IS 'Timestamp when the row was first created.';
COMMENT ON COLUMN sectors.created_by        IS 'User ID (JWT sub) who created the row.';
COMMENT ON COLUMN sectors.last_modified_at  IS 'Timestamp of the most recent update.';
COMMENT ON COLUMN sectors.last_modified_by  IS 'User ID (JWT sub) who last modified the row.';