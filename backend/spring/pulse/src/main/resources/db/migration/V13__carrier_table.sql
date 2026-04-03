CREATE TABLE carriers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    radius INTEGER NOT NULL,
    rat_id BIGINT,
    band_id BIGINT,

    CONSTRAINT fk_carriers_rat
      FOREIGN KEY (rat_id)
          REFERENCES rat (id),

    CONSTRAINT fk_carriers_band
      FOREIGN KEY (band_id)
          REFERENCES bands (id),

    CONSTRAINT uq_carriers_name
      UNIQUE (name),

    CONSTRAINT chk_carriers_radius_min
      CHECK (radius >= 0)
);

CREATE INDEX idx_carriers_rat_id ON carriers(rat_id);
CREATE INDEX idx_carriers_band_id ON carriers(band_id);