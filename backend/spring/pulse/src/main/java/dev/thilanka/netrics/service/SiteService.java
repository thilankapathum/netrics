package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.entity.Site;

import java.util.List;

public interface SiteService {

    Site createSite(Site site);

    SiteDto createSite(SiteDto dto);

    List<SiteDto> createSites(List<SiteDto> dtos);

    Site findBySiteCode(String siteCode);

    SiteDto getBySiteCode(String siteCode);


}
