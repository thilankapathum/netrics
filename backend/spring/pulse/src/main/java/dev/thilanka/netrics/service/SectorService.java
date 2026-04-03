package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.entity.Sector;

import java.util.List;

public interface SectorService {

    Sector createSector(Sector sector);

    SectorDto createSector(SectorDto dto);

    List<SectorDto> createSectors(List<SectorDto> dtos);

    Sector findBySectorName(String name);

    SectorDto getBySectorName(String name);

    Sector findBySiteAndIndex(String siteCode, Integer sectorIndex);

    SectorDto getBySiteAndIndex(String siteCode, Integer sectorIndex);
}
