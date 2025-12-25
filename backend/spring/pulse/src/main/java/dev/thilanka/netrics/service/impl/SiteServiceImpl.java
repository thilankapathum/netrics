package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.SiteDto;
import dev.thilanka.netrics.entity.Site;
import dev.thilanka.netrics.repository.SiteRepository;
import dev.thilanka.netrics.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {
    private final SiteRepository siteRepository;

    @Override
    public Site createSite(Site site) {
        return siteRepository.save(site);
    }

    @Override
    public SiteDto createSite(SiteDto dto) {

        Site site = Site.builder()
                .siteCode(dto.siteCode())
                .siteName(dto.siteName())
                .build();

        Site savedSite = createSite(site);

        return new SiteDto(savedSite.getSiteCode(), savedSite.getSiteName());
    }

    @Override
    public List<SiteDto> createSites(List<SiteDto> dtos) {

        List<SiteDto> savedDtos = new ArrayList<>();

        for (SiteDto dto: dtos){
            SiteDto savedDto = createSite(dto);
            savedDtos.add(savedDto);
        }

        return savedDtos;
    }

    @Override
    public Site findBySiteCode(String siteCode) {

        return siteRepository.findBySiteCode(siteCode)
                .orElseThrow(()-> new RuntimeException("Site not found by " + siteCode));
    }

    @Override
    public SiteDto getBySiteCode(String siteCode) {

        Site site = findBySiteCode(siteCode);

        return new SiteDto(site.getSiteCode(),site.getSiteName());
    }
}
