package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.SectorDto;
import dev.thilanka.netrics.entity.Sector;
import dev.thilanka.netrics.entity.Site;
import dev.thilanka.netrics.repository.SectorRepository;
import dev.thilanka.netrics.service.SectorService;
import dev.thilanka.netrics.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SectorServiceImpl implements SectorService {
    private final SectorRepository sectorRepository;
    private final SiteService siteService;

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
}
