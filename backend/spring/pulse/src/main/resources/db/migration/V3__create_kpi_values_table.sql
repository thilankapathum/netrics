-- Step 1: Create table WITHOUT PRIMARY KEY constraint
CREATE TABLE kpi_values (
    id BIGSERIAL NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    cell_name VARCHAR(255) NOT NULL,
    site_name VARCHAR(255),
    kpi_value DOUBLE PRECISION,
    numerator_kpi_value DOUBLE PRECISION,
    denominator_kpi_value DOUBLE PRECISION,
    data_type VARCHAR(255),
    file_name VARCHAR(255),
    standard_kpi_id BIGINT,
    numerator_kpi_id BIGINT,
    denominator_kpi_id BIGINT,
    oss_id BIGINT,
    district_code_id BIGINT,
    rat_id BIGINT,
    granularity_id BIGINT
);

-- Step 2: Convert to hypertable
SELECT create_hypertable('kpi_values', 'timestamp', chunk_time_interval => INTERVAL '1 month', if_not_exists => TRUE);

-- Step 3: Create unique index on id (this acts as PRIMARY KEY for queries)
CREATE UNIQUE INDEX uq_kpi_values_id ON kpi_values (timestamp, id);

-- Step 4: Create your business logic unique constraint
CREATE UNIQUE INDEX uq_kpi_values
ON kpi_values (timestamp, cell_name, standard_kpi_id, oss_id, rat_id, granularity_id);

-- Step 5: Foreign keys
ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_standard_kpi FOREIGN KEY (standard_kpi_id) REFERENCES standard_kpi(id);

ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_numerator_kpi FOREIGN KEY (numerator_kpi_id) REFERENCES standard_kpi(id);

ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_denominator_kpi FOREIGN KEY (denominator_kpi_id) REFERENCES standard_kpi(id);

ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_oss FOREIGN KEY (oss_id) REFERENCES oss(id);

ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_district_code FOREIGN KEY (district_code_id) REFERENCES district_codes(id);

ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_rat FOREIGN KEY (rat_id) REFERENCES rat(id);

ALTER TABLE kpi_values
ADD CONSTRAINT fk_kpi_values_granularity FOREIGN KEY (granularity_id) REFERENCES granularity(id);

-- Step 6: Other indexes for query performance
CREATE INDEX idx_kpi_values_main
ON kpi_values (timestamp DESC, standard_kpi_id, rat_id, granularity_id, district_code_id, cell_name);

CREATE INDEX idx_kpi_values_cell_standard_rat_gran_time
ON kpi_values (timestamp DESC, cell_name, standard_kpi_id, rat_id, granularity_id);

CREATE INDEX idx_kpi_values_covering
ON kpi_values (timestamp DESC, district_code_id, standard_kpi_id, rat_id, granularity_id)
INCLUDE (kpi_value, numerator_kpi_value, denominator_kpi_value);