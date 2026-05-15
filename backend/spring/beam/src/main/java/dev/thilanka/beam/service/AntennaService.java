package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.AntennaDto;
import dev.thilanka.beam.entity.Antenna;
import dev.thilanka.beam.entity.Sector;

import java.util.List;

public interface AntennaService {

    Antenna createAntenna(Antenna antenna);

    AntennaDto createAntenna(AntennaDto dto);

    List<AntennaDto> createAntennas(List<AntennaDto> dtos);

    Antenna updateAntenna(Antenna antenna);

    AntennaDto updateAntenna(AntennaDto dto);

    List<AntennaDto> updateAntennas(List<AntennaDto> dtos);

    Antenna deleteAntenna(Antenna antenna);

    AntennaDto deleteAntenna(AntennaDto dto);

    Antenna findById(Long id);

    AntennaDto getById(Long id);

    List<Antenna> findBySectorName(Sector sector);

    List<AntennaDto> getBySectorName(String sectorName);
}
