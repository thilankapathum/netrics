package dev.thilanka.beam.service;

import dev.thilanka.beam.entity.AntennaType;

public interface AntennaTypeService {
    AntennaType findByName(String name);
}
