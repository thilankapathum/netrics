CREATE TABLE alarm_field_mappings
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alarm_source_id  BIGINT      NOT NULL,
    canonical_field  VARCHAR(50) NOT NULL,
    source_column    VARCHAR(255), -- null if the source doesn't provide this field at all
    extraction_regex VARCHAR(255), -- optional; capture group 1 is extracted from the raw cell value
    default_value    VARCHAR(255), -- used when source_column is null/blank
    is_required      BOOLEAN     NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_afm_alarm_source
        FOREIGN KEY (alarm_source_id) REFERENCES alarm_sources (id),

    CONSTRAINT uq_afm_source_field
        UNIQUE (alarm_source_id, canonical_field)
);

CREATE INDEX idx_afm_alarm_source_id ON alarm_field_mappings (alarm_source_id);

-----------------------

CREATE TABLE alarm_value_mappings
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alarm_source_id BIGINT       NOT NULL,
    mapping_type    VARCHAR(20)  NOT NULL, -- SEVERITY, ACK_STATE, CLEAR_STATE
    raw_value       VARCHAR(100) NOT NULL,
    canonical_value VARCHAR(50)  NOT NULL,

    CONSTRAINT fk_avm_alarm_source
        FOREIGN KEY (alarm_source_id) REFERENCES alarm_sources (id),

    CONSTRAINT uq_avm_source_type_raw
        UNIQUE (alarm_source_id, mapping_type, raw_value)
);

CREATE INDEX idx_avm_lookup ON alarm_value_mappings (alarm_source_id, mapping_type);

-- Tracks raw values seen with no mapping to review
-- without them silently becoming INDETERMINATE/UNKNOWN forever.
CREATE TABLE unmapped_alarm_values
(
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alarm_source_id  BIGINT       NOT NULL,
    mapping_type     VARCHAR(20)  NOT NULL,
    raw_value        VARCHAR(255) NOT NULL,
    first_seen       TIMESTAMP    NOT NULL DEFAULT now(),
    last_seen        TIMESTAMP    NOT NULL DEFAULT now(),
    occurrence_count INT          NOT NULL DEFAULT 1,

    CONSTRAINT fk_uav_alarm_source
        FOREIGN KEY (alarm_source_id) REFERENCES alarm_sources (id),

    CONSTRAINT uq_uav_source_type_raw
        UNIQUE (alarm_source_id, mapping_type, raw_value)
);