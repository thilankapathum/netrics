package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.common.enums.CsvImportStatus;
import dev.thilanka.beam.common.exception.ResourceNotFoundException;
import dev.thilanka.beam.dto.SiteCsvImportResultDto;
import dev.thilanka.beam.dto.SiteDto;
import dev.thilanka.beam.dto.SiteEvent;
import dev.thilanka.beam.dto.SiteUpdateResult;
import dev.thilanka.beam.entity.InfraType;
import dev.thilanka.beam.entity.Operator;
import dev.thilanka.beam.entity.Site;
import dev.thilanka.beam.repository.SiteRepository;
import dev.thilanka.beam.service.BeamEventPublisher;
import dev.thilanka.beam.service.InfraTypeService;
import dev.thilanka.beam.service.OperatorService;
import dev.thilanka.beam.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {
    private final SiteRepository siteRepository;
    private final BeamEventPublisher eventPublisher;
    private final OperatorService operatorService;
    private final InfraTypeService infraTypeService;

    private Integer siteCountWithMissingInfo = 0;

    @Override
    public Site createSite(Site site) {
        Site savedSite = siteRepository.save(site);
        eventPublisher.publishSiteEvent(buildSiteEvent("CREATED", savedSite));
        return savedSite;
    }

    @Override
    public SiteDto createSite(SiteDto dto) {
        return toSiteDto(createSite(toSite(dto)));
    }

    @Override
    public List<SiteDto> createSites(List<SiteDto> dtos) {
        List<SiteDto> createdSites = new ArrayList<>();
        for (SiteDto dto : dtos) {
            try {
                createdSites.add(createSite(dto));
            } catch (Exception e) {
                log.warn("Failed to save site {}", dto.siteCode());
            }
        }
        return createdSites;
    }

    @Override
    public Site findBySiteCode(String siteCode) {
        return siteRepository.findBySiteCode(siteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "siteCode", siteCode));
    }

    @Override
    public List<Site> findAllSites() {
        return siteRepository.findAllByOrderBySiteCodeAsc();
    }

    @Override
    public List<SiteDto> getAllSites() {
        return findAllSites().stream().map(this::toSiteDto).collect(Collectors.toList());
    }

    @Override
    public List<Site> findSitesWithMissingInfo() {
        return siteRepository.findSitesWithMissingInfo();
    }

    @Override
    public List<SiteDto> getSitesWithMissingInfo() {
        return findSitesWithMissingInfo().stream().map(this::toSiteDto).collect(Collectors.toList());
    }

    @Override
    public Integer reloadSiteCountWithMissingInfo() {
        this.siteCountWithMissingInfo = siteRepository.findSiteCountWithMissingInfo();
        return siteCountWithMissingInfo;
    }

    @Override
    public Integer getSiteCountWithMissingInfo() {
        return this.siteCountWithMissingInfo;
    }

    @Override
    public Site updateSite(Site site) {
        Site existingSite = findBySiteCode(site.getSiteCode());

        existingSite.setSiteName(site.getSiteName());
        existingSite.setLatitude(site.getLatitude());
        existingSite.setLongitude(site.getLongitude());
        existingSite.setBuildingHeight(site.getBuildingHeight());
        existingSite.setTowerHeight(site.getTowerHeight());
        existingSite.setOperator(site.getOperator());
        existingSite.setInfraType(site.getInfraType());

        Site updatedSite = siteRepository.save(existingSite);

        log.info("Publishing event UPDATED for {}", updatedSite.getSiteCode());
        eventPublisher.publishSiteEvent(buildSiteEvent("UPDATED", updatedSite));  // KAFKA EVENT PUBLISH

        return updatedSite;
    }

    @Override
    public SiteUpdateResult updateSite(SiteDto dto) {

        List<String> warnings = new ArrayList<>();

        Site site = findBySiteCode(dto.siteCode());

        if (dto.siteName() != null && !dto.siteName().isEmpty()) {
            site.setSiteName(dto.siteName());
        } else {
            warnings.add("Site name not specified");
        }
        if (dto.latitude() != null && !dto.latitude().isNaN()) {
            site.setLatitude(dto.latitude());
        } else {
            warnings.add("Invalid Latitude '" + dto.latitude() + "'");
        }
        if (dto.longitude() != null && !dto.longitude().isNaN()) {
            site.setLongitude(dto.longitude());
        } else {
            warnings.add("Invalid Longitude '" + dto.longitude() + "'");
        }
        if (dto.buildingHeight() != null) {
            site.setBuildingHeight(dto.buildingHeight());
        } else {
            warnings.add("Building Height not specified");
        }
        if (dto.towerHeight() != null) {
            site.setTowerHeight(dto.towerHeight());
        } else {
            warnings.add("Tower Height not specified");
        }
        try {
            Operator operator = operatorService.findByName(dto.operatorName());
            site.setOperator(operator);
        } catch (Exception e) {
            log.warn("Operator not found by {}", dto.operatorName());
            warnings.add("Operator not found by '" + dto.operatorName() + "'");
        }
        try {
            InfraType infraType = infraTypeService.extractInfraType(dto.infraType());
            site.setInfraType(infraType);
        } catch (Exception e) {
            log.warn("InfraType not found by {}", dto.infraType());
            warnings.add("InfraType not found by '" + dto.infraType() + "'");
        }

        Site updatedSite = siteRepository.save(site);
        log.info("Publishing event UPDATED for {}", updatedSite.getSiteCode());
        eventPublisher.publishSiteEvent(buildSiteEvent("UPDATED", updatedSite));
        return new SiteUpdateResult(toSiteDto(updatedSite), warnings);
    }

    @Override
    public List<SiteDto> updateSites(List<SiteDto> siteDtos) {
        List<SiteDto> updatedSites = new ArrayList<>();
        for (SiteDto dto : siteDtos) {
            try {
                updatedSites.add(updateSite(dto).siteDto());
            } catch (Exception e) {
                log.warn("Error updating site: {} | {}", dto.siteCode(), e.getMessage());
            }
        }
        return updatedSites;
    }

    @Override
    public List<SiteCsvImportResultDto> updateSitesWithResults(List<SiteDto> dtos) {
        List<SiteCsvImportResultDto> importResultDtos = new ArrayList<>();

        for (SiteDto dto : dtos) {
            try {
                SiteUpdateResult result = updateSite(dto);
                String errorMessage = String.join(", ", result.warnings());
                if (result.warnings().isEmpty()) {
                    importResultDtos.add(new SiteCsvImportResultDto(result.siteDto(), CsvImportStatus.SUCCESS, ""));
                } else {
                    importResultDtos.add(new SiteCsvImportResultDto(result.siteDto(), CsvImportStatus.PARTIAL_SUCCESS, errorMessage));
                }
            } catch (ResourceNotFoundException e) {     //-- If no site exist by SiteCode
                log.warn(e.getMessage());
                try {
                    SiteDto newSite = createSite(dto);
                    importResultDtos.add(new SiteCsvImportResultDto(newSite, CsvImportStatus.SUCCESS, "New site created"));
                } catch (Exception ex) {
                    log.warn("Error creating new site {} | {}", dto.siteCode(), ex.getMessage());
                    importResultDtos.add(new SiteCsvImportResultDto(dto, CsvImportStatus.FAIL, ex.getMessage()));
                }
            } catch (Exception e) {
                log.warn("Error updating site {} | {}", dto.siteCode(), e.getMessage());
                importResultDtos.add(new SiteCsvImportResultDto(dto, CsvImportStatus.FAIL, e.getMessage()));
            }
        }
        return importResultDtos;
    }

    private SiteEvent buildSiteEvent(String type, Site site) {
        return new SiteEvent(type,
                site.getSiteCode(),
                site.getSiteName(),
                site.getLatitude(),
                site.getLongitude(),
                site.getCreatedAt(),
                site.getLastModifiedAt(),
                site.getCreatedBy(),
                site.getLastModifiedBy());
    }

    private SiteDto toSiteDto(Site site) {
        String operatorName = "";
        String infraTypeName = "";

        if (site.getOperator() != null) {
            operatorName = site.getOperator().getName();
        }
        if (site.getInfraType() != null) {
            infraTypeName = site.getInfraType().getInfraType() + "-" + site.getInfraType().getLegType();
        }

        return new SiteDto(site.getSiteCode(), site.getSiteName(), site.getLatitude(), site.getLongitude(),
                site.getBuildingHeight(), site.getTowerHeight(), operatorName, infraTypeName);
    }

    private Site toSite(SiteDto dto) {
        Operator operator;
        InfraType infraType;

        try {
            operator = operatorService.findByName(dto.operatorName());
        } catch (Exception e) {
            log.warn("Operator not found by {}", dto.operatorName());
            operator = null;
        }

        try {
            infraType = infraTypeService.extractInfraType(dto.infraType());
        } catch (Exception e) {
            log.warn("InfraType not found by {}", dto.infraType());
            infraType = null;
        }

        return Site.builder()
                .siteCode(dto.siteCode())
                .siteName(dto.siteName())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .buildingHeight(dto.buildingHeight())
                .towerHeight(dto.towerHeight())
                .operator(operator)
                .infraType(infraType)
                .build();
    }
}
