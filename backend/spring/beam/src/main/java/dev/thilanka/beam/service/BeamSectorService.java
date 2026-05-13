package dev.thilanka.beam.service;

import dev.thilanka.beam.entity.BeamSector;

public interface BeamSectorService {
    BeamSector findByName(String name);
}
