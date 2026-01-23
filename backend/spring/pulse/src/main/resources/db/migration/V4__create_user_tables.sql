CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255)
);

CREATE UNIQUE INDEX idx_users_user_id ON users(user_id);
CREATE UNIQUE INDEX idx_users_username ON users(username);


-- USER - AREA MAPPING --
CREATE TABLE user_area_mapping (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    area_id BIGINT NOT NULL,
    CONSTRAINT uq_user_area UNIQUE (user_id, area_id),
    CONSTRAINT fk_user_area_mapping_area FOREIGN KEY (area_id)
        REFERENCES areas(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_area_mapping_user_id ON user_area_mapping(user_id);

