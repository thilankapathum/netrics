-- Antennas whose electrical tilt is set remotely (RET); these carry no manual electrical_tilts rows
ALTER TABLE antennas ADD COLUMN ret_available BOOLEAN NOT NULL DEFAULT FALSE;
