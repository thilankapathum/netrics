package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.SiteCsvImportResultDto;
import dev.thilanka.beam.dto.SiteDto;
import dev.thilanka.beam.dto.SiteUpdateResult;
import dev.thilanka.beam.entity.Site;

import java.util.List;

public interface SiteService {

    Site createSite(Site site);

    SiteDto createSite(SiteDto dto);

    List<SiteDto> createSites(List<SiteDto> dtos);

    Site findBySiteCode(String siteCode);

    List<Site> findAllSites();

    List<SiteDto> getAllSites();

    List<Site> findSitesWithMissingInfo();

    List<SiteDto> getSitesWithMissingInfo();

    Integer reloadSiteCountWithMissingInfo();

    Integer getSiteCountWithMissingInfo();

    Site updateSite(Site site);

    SiteUpdateResult updateSite(SiteDto siteDto);

    List<SiteDto> updateSites(List<SiteDto> siteDtos);

    List<SiteCsvImportResultDto> updateSitesWithResults(List<SiteDto> dtos);


}
