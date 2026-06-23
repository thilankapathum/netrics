package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.entity.Site;

import java.util.List;

public interface SiteService {

//    Site createSite(Site site);

//    SiteDto createSite(SiteDto dto);

//    List<SiteDto> createSites(List<SiteDto> dtos);

    Site findBySiteCode(String siteCode);

    SiteDto getBySiteCode(String siteCode);

    List<Site> findSitesWithMissingInfo();

    List<SiteDto> getSitesWithMissingInfo();

    List<Site> findAllSites();

    List<SiteDto> getAllSites();

//    Site updateSite(Site site);

//    SiteUpdateResult updateSite(SiteDto siteDto);

//    List<SiteDto> updateSites(List<SiteDto> siteDtos);

//    List<SiteCsvImportResultDto> updateSitesWithResults(List<SiteDto> dtos);

//    Integer reloadSiteCountWithMissingInfo();
//
//    Integer getSiteCountWithMissingInfo();

    void reloadSites();

    List<SiteDto> searchSites(String siteCode);


}
