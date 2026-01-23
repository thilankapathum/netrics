-- GRANULARITY --
CREATE TABLE granularity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL UNIQUE,
    plus_seconds INT
);

CREATE INDEX idx_granularity_name_label
ON granularity (name, label);

-- DISTRICTS --
CREATE TABLE districts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE
);

CREATE INDEX idx_districts_name_code
ON districts (name, code);

-- DISTRICT CODES --
CREATE TABLE district_codes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    category VARCHAR(255),
    district_id BIGINT
);

ALTER TABLE district_codes
ADD CONSTRAINT fk_district
FOREIGN KEY (district_id)
REFERENCES districts(id);

CREATE INDEX idx_district_codes_code_district
ON district_codes (code, district_id);

-- AREA TYPES --
CREATE TABLE area_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE
);

CREATE INDEX idx_area_types_name
ON area_types (name);

-- AREAS --
CREATE TABLE areas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    area_type_id BIGINT
);

ALTER TABLE areas
ADD CONSTRAINT fk_area_area_type
FOREIGN KEY (area_type_id)
REFERENCES area_types(id);

CREATE INDEX idx_areas_name
ON areas(name);

-- AREA - DISTRICT CODE MAPPING --
CREATE TABLE area_district_code_mapping (
    id BIGSERIAL PRIMARY KEY,
    area_id BIGINT NOT NULL,
    district_code_id BIGINT NOT NULL,
    CONSTRAINT uq_area_district_code UNIQUE (area_id, district_code_id)
);

ALTER TABLE area_district_code_mapping
ADD CONSTRAINT fk_area_mapping_area
FOREIGN KEY (area_id)
REFERENCES areas(id);

ALTER TABLE area_district_code_mapping
ADD CONSTRAINT fk_area_mapping_district_code
FOREIGN KEY (district_code_id)
REFERENCES district_codes(id);

CREATE INDEX idx_area_mapping_area_id
ON area_district_code_mapping(area_id);

CREATE INDEX idx_area_mapping_district_code_id
ON area_district_code_mapping(district_code_id);

-- OSS --
CREATE TABLE oss (
    id BIGSERIAL PRIMARY KEY,
    oss_name VARCHAR(255) NOT NULL,
    identifier VARCHAR(255) NOT NULL UNIQUE,
    vendor VARCHAR(255)
);

CREATE INDEX idx_oss_name
ON oss(oss_name);

-- RAT --
CREATE TABLE rat (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    label VARCHAR(255) NOT NULL UNIQUE
);

CREATE INDEX idx_rat_name
ON rat(name);

CREATE INDEX idx_rat_label
ON rat(label);

-- BANDS --
CREATE TABLE bands (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    number INT NOT NULL UNIQUE,
    unit VARCHAR(255) NOT NULL
);

CREATE INDEX idx_bands_name
ON bands(name);

CREATE INDEX idx_bands_number
ON bands(number);

-- SITES --
CREATE TABLE sites (
    id BIGSERIAL PRIMARY KEY,
    site_code VARCHAR(255) NOT NULL UNIQUE,
    site_name VARCHAR(255) NOT NULL
);

CREATE INDEX idx_sites_site_code
ON sites(site_code);

-- BASIC KPI --
CREATE TABLE basic_kpi (
    id BIGSERIAL PRIMARY KEY,
    kpi_name VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    worst_order VARCHAR(255),
    threshold DOUBLE PRECISION,
    aggregation VARCHAR(255),
    unit VARCHAR(255),
    rat_id BIGINT,
    CONSTRAINT uq_basic_kpi UNIQUE (kpi_name, label, rat_id)
);

ALTER TABLE basic_kpi
ADD CONSTRAINT fk_basic_kpi_rat
FOREIGN KEY (rat_id)
REFERENCES rat(id);

CREATE INDEX idx_basic_kpi_name
ON basic_kpi(kpi_name);

CREATE INDEX idx_basic_kpi_label
ON basic_kpi(label);

-- STANDARD KPI --
CREATE TABLE standard_kpi (
    id BIGSERIAL PRIMARY KEY,
    kpi_name VARCHAR(255) NOT NULL,
    label VARCHAR(255) NOT NULL,
    unit VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    worst_order VARCHAR(255),
    threshold DOUBLE PRECISION,
    aggregation VARCHAR(255),
    basic_kpi_id BIGINT,
    rat_id BIGINT,
    CONSTRAINT uq_standard_kpi UNIQUE (kpi_name, label, rat_id)
);

ALTER TABLE standard_kpi
ADD CONSTRAINT fk_standard_kpi_basic_kpi
FOREIGN KEY (basic_kpi_id)
REFERENCES basic_kpi(id);

ALTER TABLE standard_kpi
ADD CONSTRAINT fk_standard_kpi_rat
FOREIGN KEY (rat_id)
REFERENCES rat(id);

CREATE INDEX idx_standard_kpi_name
ON standard_kpi(kpi_name);

CREATE INDEX idx_standard_kpi_label
ON standard_kpi(label);

-- STANDARD - RAW KPI MAPPING --
CREATE TABLE standard_raw_kpi_mapping (
    id BIGSERIAL PRIMARY KEY,
    standard_kpi_id BIGINT,
    numerator_id BIGINT,
    denominator_id BIGINT,
    rat_id BIGINT,
    CONSTRAINT uq_standard_raw_kpi UNIQUE (standard_kpi_id, numerator_id, denominator_id)
);

ALTER TABLE standard_raw_kpi_mapping
ADD CONSTRAINT fk_raw_kpi_standard_kpi
FOREIGN KEY (standard_kpi_id)
REFERENCES standard_kpi(id);

ALTER TABLE standard_raw_kpi_mapping
ADD CONSTRAINT fk_raw_kpi_numerator
FOREIGN KEY (numerator_id)
REFERENCES standard_kpi(id);

ALTER TABLE standard_raw_kpi_mapping
ADD CONSTRAINT fk_raw_kpi_denominator
FOREIGN KEY (denominator_id)
REFERENCES standard_kpi(id);

ALTER TABLE standard_raw_kpi_mapping
ADD CONSTRAINT fk_raw_kpi_rat
FOREIGN KEY (rat_id)
REFERENCES rat(id);

CREATE INDEX idx_raw_kpi_standard_kpi
ON standard_raw_kpi_mapping(standard_kpi_id);

CREATE INDEX idx_raw_kpi_numerator
ON standard_raw_kpi_mapping(numerator_id);

CREATE INDEX idx_raw_kpi_denominator
ON standard_raw_kpi_mapping(denominator_id);


-- KPI MAPPING TO OSS --
CREATE TABLE kpi_mapping (
    id BIGSERIAL PRIMARY KEY,
    oss_kpi_name VARCHAR(255),
    multiplication_factor DOUBLE PRECISION DEFAULT 1.0,
    oss_id BIGINT,
    standard_kpi_id BIGINT,
    rat_id BIGINT,
    CONSTRAINT uq_kpi_mapping UNIQUE (standard_kpi_id, oss_id, rat_id)
);

ALTER TABLE kpi_mapping
ADD CONSTRAINT fk_kpi_mapping_oss
FOREIGN KEY (oss_id)
REFERENCES oss(id);

ALTER TABLE kpi_mapping
ADD CONSTRAINT fk_kpi_mapping_standard_kpi
FOREIGN KEY (standard_kpi_id)
REFERENCES standard_kpi(id);

ALTER TABLE kpi_mapping
ADD CONSTRAINT fk_kpi_mapping_rat
FOREIGN KEY (rat_id)
REFERENCES rat(id);

CREATE INDEX idx_kpi_mapping_oss
ON kpi_mapping(oss_id);

CREATE INDEX idx_kpi_mapping_standard_kpi
ON kpi_mapping(standard_kpi_id);

CREATE INDEX idx_kpi_mapping_rat
ON kpi_mapping(rat_id);


-- CELL --
CREATE TABLE cells (
    id BIGSERIAL PRIMARY KEY,
    cell_name VARCHAR(255) NOT NULL UNIQUE,
    node_name VARCHAR(255),
    rat_id BIGINT,
    site_id BIGINT,
    band_id BIGINT
);

ALTER TABLE cells
ADD CONSTRAINT fk_cells_rat
FOREIGN KEY (rat_id)
REFERENCES rat(id);

ALTER TABLE cells
ADD CONSTRAINT fk_cells_site
FOREIGN KEY (site_id)
REFERENCES sites(id);

ALTER TABLE cells
ADD CONSTRAINT fk_cells_band
FOREIGN KEY (band_id)
REFERENCES bands(id);

CREATE INDEX idx_cells_cell_name
ON cells(cell_name);

CREATE INDEX idx_cells_rat_id
ON cells(rat_id);

CREATE INDEX idx_cells_site_id
ON cells(site_id);

CREATE INDEX idx_cells_band_id
ON cells(band_id);


-- CELL NAMES --
CREATE TABLE cell_names (
    id BIGSERIAL PRIMARY KEY,
    cell_name VARCHAR(255) NOT NULL UNIQUE,
    rat_name VARCHAR(255) NOT NULL,
    rat_label VARCHAR(255) NOT NULL
);

CREATE INDEX idx_cell_names_rat_name
ON cell_names(rat_name);

CREATE INDEX idx_cell_names_rat_label
ON cell_names(rat_label);
