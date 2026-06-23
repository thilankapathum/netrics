package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.SectorCsvImportResultDto;
import dev.thilanka.beam.dto.SectorDto;
import dev.thilanka.beam.dto.SectorUpdateResult;
import dev.thilanka.beam.entity.Sector;

import java.util.List;

public interface SectorService {
    Sector findByName(String name);

    Sector createSector(Sector sector);

    SectorDto createSector(SectorDto dto);

    List<SectorDto> createSectors(List<SectorDto> dtos);

    List<Sector> findAllSectors();

    List<SectorDto> getAllSectors();

    List<Sector> findSectorsWithMissingInfo();

    List<SectorDto> getSectorsWithMissingInfo();

    Sector updateSector(Sector sector);

    SectorUpdateResult updateSector(SectorDto dto);

    List<SectorCsvImportResultDto> updateSectorsWithResults(List<SectorDto> dtos);

    Integer reloadSectorCountWithMissingInfo();

    Integer getSectorCountWithMissingInfo();
}
