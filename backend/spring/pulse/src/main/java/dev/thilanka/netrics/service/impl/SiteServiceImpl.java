package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.SiteCsvImportResultDto;
import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.dto.SiteUpdateResult;
import dev.thilanka.netrics.entity.Site;
import dev.thilanka.netrics.entity.enums.CsvImportStatus;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.SiteRepository;
import dev.thilanka.netrics.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {
    private final SiteRepository siteRepository;
    private final Mapper mapper;

    private Integer siteCountWithMissingInfo = 0;

    @Override
    public Site createSite(Site site) {
        return siteRepository.save(site);
    }

    @Override
    public SiteDto createSite(SiteDto dto) {

        Site site = Site.builder()
                .siteCode(dto.siteCode())
                .siteName(dto.siteName())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .build();

        Site savedSite = createSite(site);

        return new SiteDto(savedSite.getSiteCode(), savedSite.getSiteName(), savedSite.getLatitude(), savedSite.getLongitude());
    }

    @Override
    public List<SiteDto> createSites(List<SiteDto> dtos) {

        List<SiteDto> savedDtos = new ArrayList<>();

        for (SiteDto dto : dtos) {
            try {
                SiteDto savedDto = createSite(dto);
                savedDtos.add(savedDto);
            } catch (Exception e) {
                log.warn("Error creating site: {}{}", dto.siteCode(), e.getMessage());
            }
        }

        return savedDtos;
    }

    @Override
    public Site findBySiteCode(String siteCode) {

        return siteRepository.findBySiteCode(siteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Site", "Site ID", siteCode));
    }

    @Override
    public SiteDto getBySiteCode(String siteCode) {

        Site site = findBySiteCode(siteCode);

        return new SiteDto(site.getSiteCode(), site.getSiteName(), site.getLatitude(), site.getLongitude());
    }

    @Override
    public List<Site> findSitesWithMissingInfo() {
        return siteRepository.findSitesWithMissingInfo();
    }

    @Override
    public List<SiteDto> getSitesWithMissingInfo() {
        List<Site> sites = findSitesWithMissingInfo();

        return sites.stream().map(mapper::siteToDto).collect(Collectors.toList());
    }

    @Override
    public List<Site> findAllSites() {
        return siteRepository.findAll();
    }

    @Override
    public List<SiteDto> getAllSites() {
        List<Site> sites = findAllSites();

        return sites.stream().map(mapper::siteToDto).collect(Collectors.toList());
    }

    @Override
    public Site updateSite(Site site) {
        Optional<Site> existingSite = siteRepository.findBySiteCode(site.getSiteCode());

        if (existingSite.isPresent()) {

            Site updatingSite = existingSite.get();

            if (!site.getSiteName().isEmpty()) {
                updatingSite.setSiteName(site.getSiteName());
            }
            if (!site.getLatitude().isNaN()) {
                updatingSite.setLatitude(site.getLatitude());
            }
            if (!site.getLongitude().isNaN()) {
                updatingSite.setLongitude(site.getLongitude());
            }

            return siteRepository.save(updatingSite);

        } else throw new ResourceNotFoundException("Site", "Site ID", site.getSiteCode());

//        existingSite.ifPresent(s -> site.setId(s.getId()));
//        return siteRepository.save(updatin);
    }

    @Override
    public SiteUpdateResult updateSite(SiteDto siteDto) {

        List<String> warnings = new ArrayList<>();

        Site site = siteRepository.findBySiteCode(siteDto.siteCode())
                .orElseThrow(() -> new ResourceNotFoundException("Site", "Site ID", siteDto.siteCode()));

        if (siteDto.siteName() != null && !siteDto.siteName().isEmpty()) {
            site.setSiteName(siteDto.siteName());
        } else {
            warnings.add("Site Name not specified");
        }

        if (siteDto.latitude() != null && !siteDto.latitude().isNaN()) {
            site.setLatitude(siteDto.latitude());
        } else {
            warnings.add("Invalid Latitude '" + siteDto.latitude() + "'");
        }

        if (siteDto.longitude() != null && !siteDto.longitude().isNaN()) {
            site.setLongitude(siteDto.longitude());
        } else {
            warnings.add("Invalid Longitude '" + siteDto.longitude() + "'");
        }

        Site updatedSite = siteRepository.save(site);
        return new SiteUpdateResult(mapper.siteToDto(updatedSite), warnings);
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
//                SiteDto updatedSite = updateSite(dto);
                if (result.warnings().isEmpty()) {
                    importResultDtos.add(new SiteCsvImportResultDto(result.siteDto(), CsvImportStatus.SUCCESS, ""));
                } else {
                    importResultDtos.add(new SiteCsvImportResultDto(result.siteDto(), CsvImportStatus.PARTIAL_SUCCESS, errorMessage));
                }
            } catch (ResourceNotFoundException e) {     //-- If no site exist by SiteCode
                log.warn(e.getMessage());
                SiteDto newSite = createSite(dto);
                importResultDtos.add(new SiteCsvImportResultDto(newSite, CsvImportStatus.SUCCESS, "New site created"));
            } catch (Exception e) {
                log.warn("Error updating site {} | {}", dto.siteCode(), e.getMessage());
                importResultDtos.add(new SiteCsvImportResultDto(dto, CsvImportStatus.FAIL, e.getMessage()));
            }
        }
        return importResultDtos;
    }

    @Override
    public Integer reloadSiteCountWithMissingInfo() {
        this.siteCountWithMissingInfo = siteRepository.findSiteCountWithMissingInfo();
        return this.siteCountWithMissingInfo;
    }

    @Override
    public Integer getSiteCountWithMissingInfo() {
        return this.siteCountWithMissingInfo;
    }
}
