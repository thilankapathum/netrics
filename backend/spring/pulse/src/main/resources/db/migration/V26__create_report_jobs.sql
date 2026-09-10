CREATE TABLE report_jobs
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    status        VARCHAR(20) NOT NULL,
    report_type   VARCHAR(50) NOT NULL,
    params_json   TEXT,
    file_path     TEXT,
    file_name     TEXT,
    error_message TEXT,
    requested_by  VARCHAR(255),
    created_at    TIMESTAMP   NOT NULL DEFAULT now(),
    completed_at  TIMESTAMP
);

CREATE INDEX idx_report_jobs_status ON report_jobs (status);
CREATE INDEX idx_report_jobs_created_at ON report_jobs (created_at);