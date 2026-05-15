package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.GeoUtils;
import dev.thilanka.beam.common.enums.CsvImportStatus;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.SectorCsvImportResultDto;
import dev.thilanka.beam.dto.SectorDto;
import dev.thilanka.beam.dto.SectorEvent;
import dev.thilanka.beam.dto.SectorUpdateResult;
import dev.thilanka.beam.entity.Sector;
import dev.thilanka.beam.entity.Site;
import dev.thilanka.beam.repository.SectorRepository;
import dev.thilanka.beam.service.BeamEventPublisher;
import dev.thilanka.beam.service.SectorService;
import dev.thilanka.beam.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {
    private final SectorRepository sectorRepository;
    private final BeamEventPublisher eventPublisher;
    private final SiteService siteService;
    private final GeoUtils geoUtils;

    @Override
    public Sector findByName(String name) {
        return sectorRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Beam", "name", name));
    }

    @Override
    public Sector createSector(Sector sector) {
        Sector savedSector = sectorRepository.save(sector);
        eventPublisher.publishSectorEvent(buildSectorEvent("CREATED", savedSector));
        return savedSector;
    }

    @Override
    public SectorDto createSector(SectorDto dto) {
        Sector sector = dtoToSector(dto);
        return toSectorDto(createSector(sector));
    }

    @Override
    public List<SectorDto> createSectors(List<SectorDto> dtos) {
        List<SectorDto> sectorDtos = new ArrayList<>();

        for (SectorDto dto : dtos) {
            try {
                sectorDtos.add(createSector(dto));
            } catch (Exception e) {
                log.warn("Failed creating sectror {}", dto.name(), e);
            }
        }
        return sectorDtos;
    }

    @Override
    public Sector updateSector(Sector sector) {
        Sector existingSector = findByName(sector.getName());

        existingSector.setSectorIndex(sector.getSectorIndex());
        existingSector.setAzimuth(sector.getAzimuth());
        existingSector.setSite(sector.getSite());

        Sector updatedSector = sectorRepository.save(existingSector);
        eventPublisher.publishSectorEvent(buildSectorEvent("UPDATED", updatedSector));
        return updatedSector;
    }

    @Override
    public SectorUpdateResult updateSector(SectorDto dto) {

        List<String> warnings = new ArrayList<>();

        Sector sector = findByName(dto.name());

        if(dto.sectorIndex() != null){
            sector.setSectorIndex(dto.sectorIndex());
        } else {
            warnings.add("Sector Index is null");
        }
        if (dto.azimuth() != null){
            if (geoUtils.isValidAzimuth(dto.azimuth())) {
                sector.setAzimuth(dto.azimuth());
            } else warnings.add("Invalid azimuth");
        } else warnings.add("Azimuth is null");
        if (dto.siteCode() != null){
            try {
                Site site = siteService.findBySiteCode(dto.siteCode());
                sector.setSite(site);
            } catch (ResourceNotFoundException e) {
                warnings.add("Invalid Site ID");
            }
        } else warnings.add("Site ID is null");

        Sector savedSector = sectorRepository.save(sector);
        eventPublisher.publishSectorEvent(buildSectorEvent("UPDATED", savedSector));

        return new SectorUpdateResult(toSectorDto(savedSector), warnings);
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

    private SectorEvent buildSectorEvent(String type, Sector sector) {
        return new SectorEvent(
                type,
                sector.getSectorIndex(),
                sector.getName(),
                sector.getAzimuth(),
                sector.getSite().getId(),
                sector.getCreatedAt(),
                sector.getLastModifiedAt(),
                sector.getCreatedBy(),
                sector.getLastModifiedBy()
        );
    }

    private Sector dtoToSector(SectorDto dto) {
        Site site = siteService.findBySiteCode(dto.siteCode());

        return Sector.builder()
                .sectorIndex(dto.sectorIndex())
                .name(dto.name())
                .azimuth(dto.azimuth())
                .site(site)
                .build();
    }

    private SectorDto toSectorDto(Sector sector) {
        return new SectorDto(
                sector.getSectorIndex(),
                sector.getName(),
                sector.getAzimuth(),
                sector.getSite().getSiteCode()
        );
    }
}
