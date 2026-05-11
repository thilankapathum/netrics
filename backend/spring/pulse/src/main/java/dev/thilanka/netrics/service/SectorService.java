package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.SectorCsvImportResultDto;
import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.dto.SectorUpdateResult;
import dev.thilanka.netrics.entity.Sector;

import java.util.List;

public interface SectorService {

    Sector createSector(Sector sector);

    SectorDto createSector(SectorDto dto);

    List<SectorDto> createSectors(List<SectorDto> dtos);

    Sector updateSector(Sector sector);

    SectorUpdateResult updateSector(SectorDto dto);

    List<SectorCsvImportResultDto> updateSectorsWithResults(List<SectorDto> dtos);

    Sector findBySectorName(String name);

    SectorDto getBySectorName(String name);

    Sector findBySiteAndIndex(String siteCode, Integer sectorIndex);

    SectorDto getBySiteAndIndex(String siteCode, Integer sectorIndex);

    List<SectorDto> reloadSectors();

    List<SectorDto> searchSectors(String searchString);

    List<Sector> findSectorsWithMissingInfo();

    List<SectorDto> getSectorsWithMissingInfo();

    List<Sector> findAllSectors();

    List<SectorDto> getAllSectors();

    Integer reloadSectorCountWithMissingInfo();

    Integer getSectorCountWithMissingInfo();
}
