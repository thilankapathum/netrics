package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.SectorCsvImportResultDto;
import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.dto.SectorUpdateResult;
import dev.thilanka.netrics.entity.Sector;
import dev.thilanka.netrics.entity.Site;
import dev.thilanka.netrics.entity.enums.CsvImportStatus;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.SectorRepository;
import dev.thilanka.netrics.service.SectorService;
import dev.thilanka.netrics.service.SiteService;
import dev.thilanka.netrics.util.DataTypeUtilService;
import dev.thilanka.netrics.util.GeoUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectorServiceImpl implements SectorService {
    private final SectorRepository sectorRepository;
    private final SiteService siteService;
    private final GeoUtilService geoUtilService;
    private final Mapper mapper;

    Integer sectorCountWithMissingInfo = 0;

    @Override
    public Sector createSector(Sector sector) {
        return sectorRepository.save(sector);
    }

    @Override
    public SectorDto createSector(SectorDto dto) {
        Site site = siteService.findBySiteCode(dto.siteCode());
        Sector sector = Sector.builder()
                .sectorIndex(dto.sectorIndex())
                .name(dto.name())
                .azimuth(dto.azimuth())
                .site(site)
                .build();

        Sector savedSector = createSector(sector);
        return new SectorDto(savedSector.getSectorIndex(), savedSector.getName(), savedSector.getAzimuth(), savedSector.getSite().getSiteCode());
    }

    @Override
    public List<SectorDto> createSectors(List<SectorDto> dtos) {

        List<SectorDto> sectorDtos = new ArrayList<>();

        for (SectorDto dto : dtos) {
            try {
                sectorDtos.add(createSector(dto));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return sectorDtos;
    }

    @Override
    public Sector updateSector(Sector sector) {
        Sector existingSector = sectorRepository.findByName(sector.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "Name", sector.getName()));

        existingSector.setSectorIndex(sector.getSectorIndex());
        existingSector.setName(sector.getName());
        existingSector.setAzimuth(sector.getAzimuth());
        existingSector.setSite(sector.getSite());

        return sectorRepository.save(existingSector);
    }

    @Override
    public SectorUpdateResult updateSector(SectorDto dto) {
        List<String> warnings = new ArrayList<>();

        Sector sector = sectorRepository.findByName(dto.name())
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "Name", dto.name()));

        if (dto.sectorIndex() != null) {
            sector.setSectorIndex(dto.sectorIndex());
        } else {
            warnings.add("Sector Index not specified");
        }

        if (dto.name() != null && !dto.name().isEmpty()) {
            sector.setName(dto.name());
        } else {
            warnings.add("Sector Name not specified");
        }

        if (dto.azimuth() != null) {
            if (geoUtilService.isValidAzimuth(dto.azimuth())) {
                sector.setAzimuth(dto.azimuth());
            } else {
                warnings.add("Invalid azimuth");
            }
        } else {
            warnings.add("Azimuth not specified");
        }

        if (dto.siteCode() != null) {
            try {
                Site site = siteService.findBySiteCode(dto.siteCode());
                sector.setSite(site);
            } catch (ResourceNotFoundException e) {
                log.warn(e.getMessage());
            }
        }

        Sector savedSector = sectorRepository.save(sector);

        return new SectorUpdateResult(mapper.sectorToDto(savedSector), warnings);
    }

    @Override
    public List<SectorCsvImportResultDto> updateSectorsWithResults(List<SectorDto> dtos) {
        List<SectorCsvImportResultDto> importResultDtos = new ArrayList<>();

        for (SectorDto dto : dtos) {
            try {
                SectorUpdateResult result = updateSector(dto);
                String errorMessage = String.join(", ", result.warnings());

                if (result.warnings().isEmpty()) {
                    importResultDtos.add(new SectorCsvImportResultDto(result.sectorDto(), CsvImportStatus.SUCCESS, ""));
                } else {
                    importResultDtos.add(new SectorCsvImportResultDto(result.sectorDto(), CsvImportStatus.PARTIAL_SUCCESS, errorMessage));
                }
            } catch (ResourceNotFoundException e) {
                log.warn(e.getMessage());
                SectorDto newSector = createSector(dto);
                importResultDtos.add(new SectorCsvImportResultDto(newSector, CsvImportStatus.SUCCESS, "Created new sector"));
            } catch (Exception e) {
                log.warn("Error updating sector {} | {}", dto.name(), e.getMessage());
                importResultDtos.add(new SectorCsvImportResultDto(dto, CsvImportStatus.FAIL, e.getMessage()));
            }
        }
        return importResultDtos;
    }

    @Override
    public Sector findBySectorName(String name) {
        return sectorRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "Name", name));
    }

    @Override
    public SectorDto getBySectorName(String name) {
        Sector sector = findBySectorName(name);
        return new SectorDto(sector.getSectorIndex(), sector.getName(), sector.getAzimuth(), sector.getSite().getSiteCode());
    }

    @Override
    public Sector findBySiteAndIndex(String siteCode, Integer sectorIndex) {
        Site site = siteService.findBySiteCode(siteCode);
        return sectorRepository.findBySiteIdAndSectorIndex(site.getId(), sectorIndex)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "Site ID & Sector Index", siteCode + " : " + sectorIndex));
    }

    @Override
    public SectorDto getBySiteAndIndex(String siteCode, Integer sectorIndex) {
        Sector sector = findBySiteAndIndex(siteCode, sectorIndex);
        return new SectorDto(sector.getSectorIndex(), sector.getName(), sector.getAzimuth(), sector.getSite().getSiteCode());
    }

    @Override
    public List<Sector> findSectorsWithMissingInfo() {
        return sectorRepository.findSectorsWithMissingInfo();
    }

    @Override
    public List<SectorDto> getSectorsWithMissingInfo() {
        List<Sector> sectors = findSectorsWithMissingInfo();
        return sectors.stream().map(mapper::sectorToDto).collect(Collectors.toList());
    }

    @Override
    public List<Sector> findAllSectors() {
        return sectorRepository.findAll();
    }

    @Override
    public List<SectorDto> getAllSectors() {
        List<Sector> sectors = findAllSectors();
        return sectors.stream().map(mapper::sectorToDto).collect(Collectors.toList());
    }

    @Override
    public Integer reloadSectorCountWithMissingInfo() {
        this.sectorCountWithMissingInfo = sectorRepository.findSectorCountWithMissingInfo();
        return this.sectorCountWithMissingInfo;
    }

    @Override
    public Integer getSectorCountWithMissingInfo() {
        return this.sectorCountWithMissingInfo;
    }
}
