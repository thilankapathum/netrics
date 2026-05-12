-- MANUFACTURERS --
CREATE TABLE manufacturers
(
    id               BIGSERIAL    NOT NULL,
    name             VARCHAR(255) NOT NULL,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_manufacturers PRIMARY KEY (id),
    CONSTRAINT uq_manufacturers_name UNIQUE (name)
);

CREATE UNIQUE INDEX idx_manufacturers_name
    ON manufacturers (name);


-- OPERATORS --
CREATE TABLE operators
(
    id               BIGSERIAL    NOT NULL,
    name             VARCHAR(255) NOT NULL,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_operators PRIMARY KEY (id),
    CONSTRAINT uq_operators_name UNIQUE (name)
);

CREATE UNIQUE INDEX idx_operators_name
    ON operators (name);


-- INFRA-TYPES --
CREATE TABLE infra_types
(
    id               BIGSERIAL    NOT NULL,
    infra_type       VARCHAR(255),
    leg_type         VARCHAR(255),

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_infra_types PRIMARY KEY (id)
);

CREATE INDEX idx_infra_types_infra_type
    ON infra_types (infra_type);

CREATE INDEX idx_infra_types_leg_type
    ON infra_types (leg_type);

CREATE INDEX idx_infra_types_infra_type_leg_type
    ON infra_types (infra_type, leg_type);


-- BANDS --
CREATE TABLE bands
(
    id               BIGINT       NOT NULL,
    name             VARCHAR(255) NOT NULL,
    number           INTEGER      NOT NULL,
    unit             VARCHAR(255) NOT NULL,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_bands PRIMARY KEY (id),
    CONSTRAINT uq_bands_name UNIQUE (name),
    CONSTRAINT uq_bands_number UNIQUE (number)
);

CREATE UNIQUE INDEX idx_bands_name
    ON bands (name);

CREATE UNIQUE INDEX idx_bands_number
    ON bands (number);

CREATE INDEX idx_bands_unit
    ON bands (unit);


-- ANTENNA-TYPES --
CREATE TABLE antenna_types
(
    id               BIGSERIAL    NOT NULL,
    name             VARCHAR(255) NOT NULL,
    port_count       INTEGER      NOT NULL,
    height           INTEGER,
    width            INTEGER,
    ret_availability BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_antenna_types PRIMARY KEY (id),
    CONSTRAINT uq_antenna_types_name UNIQUE (name),
    CONSTRAINT chk_antenna_types_port_count CHECK (port_count >= 1)
);

CREATE UNIQUE INDEX idx_antenna_types_name
    ON antenna_types (name);

CREATE INDEX idx_antenna_types_port_count
    ON antenna_types (port_count);

CREATE INDEX idx_antenna_types_ret_availability
    ON antenna_types (ret_availability);

CREATE INDEX idx_antenna_types_height_width
    ON antenna_types (height, width);


-- SITES --
CREATE TABLE sites
(
    id               BIGINT       NOT NULL,
    site_code        VARCHAR(255) NOT NULL,
    site_name        VARCHAR(255) NOT NULL,
    latitude         DECIMAL(9, 6),
    longitude        DECIMAL(9, 6),
    building_height  SMALLINT,
    tower_height     SMALLINT,
    operator_id      BIGINT,
    infra_type_id    BIGINT,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_sites PRIMARY KEY (id),
    CONSTRAINT uq_sites_site_code UNIQUE (site_code),
    CONSTRAINT chk_sites_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_sites_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT fk_sites_operator
        FOREIGN KEY (operator_id) REFERENCES operators (id),
    CONSTRAINT fk_sites_infra_type
        FOREIGN KEY (infra_type_id) REFERENCES infra_types (id)
);

CREATE UNIQUE INDEX idx_sites_site_code
    ON sites (site_code);

CREATE INDEX idx_sites_site_name
    ON sites (site_name);

CREATE INDEX idx_sites_operator_id
    ON sites (operator_id);

CREATE INDEX idx_sites_infra_type_id
    ON sites (infra_type_id);


-- SECTORS --
CREATE TABLE sectors
(
    id               BIGINT       NOT NULL,
    sector_index     INTEGER      NOT NULL,
    name             VARCHAR(255) NOT NULL,
    azimuth          INTEGER,
    site_id          BIGINT,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_sectors PRIMARY KEY (id),
    CONSTRAINT uq_sectors_name UNIQUE (name),
    CONSTRAINT uq_sectors_site_sector_index UNIQUE (site_id, sector_index),
    CONSTRAINT chk_sectors_sector_index CHECK (sector_index >= 1),
    CONSTRAINT chk_sectors_azimuth CHECK (azimuth BETWEEN 0 AND 360),
    CONSTRAINT fk_sectors_site
        FOREIGN KEY (site_id) REFERENCES sites (id)
);

CREATE UNIQUE INDEX idx_sectors_name
    ON sectors (name);

CREATE INDEX idx_sectors_site_id
    ON sectors (site_id);

CREATE UNIQUE INDEX idx_sectors_site_id_sector_index
    ON sectors (site_id, sector_index);

CREATE INDEX idx_sectors_azimuth
    ON sectors (azimuth);


-- ANTENNAS --
CREATE TABLE antennas
(
    id               BIGSERIAL    NOT NULL,
    antenna_index    SMALLINT     NOT NULL,
    azimuth          SMALLINT     NOT NULL,
    mechanical_tilt  SMALLINT     NOT NULL,
    antenna_height   SMALLINT,
    antenna_type_id  BIGINT,
    sector_id        BIGINT,
    manufacturer_id  BIGINT,

    created_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),

    CONSTRAINT pk_antennas PRIMARY KEY (id),
    CONSTRAINT uq_antennas_sector_antenna_index UNIQUE (sector_id, antenna_index),
    CONSTRAINT chk_antennas_antenna_index CHECK (antenna_index >= 1),
    CONSTRAINT chk_antennas_azimuth CHECK (azimuth BETWEEN 0 AND 360),
    CONSTRAINT chk_antennas_mechanical_tilt CHECK (mechanical_tilt BETWEEN -90 AND 90),
    CONSTRAINT chk_antennas_antenna_height CHECK (antenna_height BETWEEN 0 AND 1000),
    CONSTRAINT fk_antennas_antenna_type
        FOREIGN KEY (antenna_type_id) REFERENCES antenna_types (id),
    CONSTRAINT fk_antennas_sector
        FOREIGN KEY (sector_id) REFERENCES sectors (id),
    CONSTRAINT fk_antennas_manufacturer
        FOREIGN KEY (manufacturer_id) REFERENCES manufacturers (id)
);

CREATE UNIQUE INDEX idx_antennas_sector_id_antenna_index
    ON antennas (sector_id, antenna_index);

CREATE INDEX idx_antennas_sector_id
    ON antennas (sector_id);

CREATE INDEX idx_antennas_antenna_type_id
    ON antennas (antenna_type_id);

CREATE INDEX idx_antennas_manufacturer_id
    ON antennas (manufacturer_id);

CREATE INDEX idx_antennas_azimuth
    ON antennas (azimuth);

CREATE INDEX idx_antennas_mechanical_tilt
    ON antennas (mechanical_tilt);


-- ELECTRICAL TILTS --
CREATE TABLE electrical_tilts
(
    id                BIGSERIAL    NOT NULL,
    electrical_tilt   SMALLINT,
    antenna_id        BIGINT,
    supported_band_id BIGINT,

    created_at        TIMESTAMP    NOT NULL,
    created_by        VARCHAR(255) NOT NULL,
    last_modified_at  TIMESTAMP,
    last_modified_by  VARCHAR(255),

    CONSTRAINT pk_electrical_tilts PRIMARY KEY (id),
    CONSTRAINT chk_electrical_tilts_tilt CHECK (electrical_tilt BETWEEN -90 AND 90),
    CONSTRAINT fk_electrical_tilts_antenna
        FOREIGN KEY (antenna_id) REFERENCES antennas (id),
    CONSTRAINT fk_electrical_tilts_band
        FOREIGN KEY (supported_band_id) REFERENCES bands (id)
);

CREATE INDEX idx_electrical_tilts_antenna_id
    ON electrical_tilts (antenna_id);

CREATE INDEX idx_electrical_tilts_supported_band_id
    ON electrical_tilts (supported_band_id);

CREATE INDEX idx_electrical_tilts_electrical_tilt
    ON electrical_tilts (electrical_tilt);