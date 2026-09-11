package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.AntennaTypeDto;
import dev.thilanka.beam.entity.AntennaType;

import java.util.List;

public interface AntennaTypeService {

    AntennaType createAntennaType(AntennaType antennaType);

    AntennaTypeDto createAntennaType(AntennaTypeDto dto);

    List<AntennaTypeDto> createAntennaTypes(List<AntennaTypeDto> dtos);

    AntennaType updateAntennaType(AntennaType antennaType);

    AntennaTypeDto updateAntennaType(AntennaTypeDto dto);

    List<AntennaTypeDto> updateAntennaTypes(List<AntennaTypeDto> dtos);

    void deleteAntennaType(String name);

    AntennaType findByName(String name);

    AntennaTypeDto getByName(String name);

    List<AntennaType> findAll();

    List<AntennaTypeDto> getAll();
}
