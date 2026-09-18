package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.dto.SiteEngineeringSummaryDto;
import dev.thilanka.beam.dto.SiteEngineeringSummaryDto.AntennaSummary;
import dev.thilanka.beam.dto.SiteEngineeringSummaryDto.SectorSummary;
import dev.thilanka.beam.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Site -> sectors -> antennas -> electrical tilts in one response, so callers (e.g. the pulse
 * assistant) make one HTTP call instead of one per sector and antenna.
 * Lives outside SiteServiceImpl because SectorServiceImpl already depends on SiteService.
 */
@Service
@RequiredArgsConstructor
public class SiteEngineeringSummaryServiceImpl implements SiteEngineeringSummaryService {
    private final SiteService siteService;
    private final SectorService sectorService;
    private final AntennaService antennaService;
    private final ElectricalTiltService electricalTiltService;

    @Override
    @Transactional(readOnly = true)
    public SiteEngineeringSummaryDto getBySiteCode(String siteCode) {
        // ponytail: one query per sector/antenna; fine for site-sized data, switch to a join if it shows in slow logs
        return new SiteEngineeringSummaryDto(
                siteService.getBySiteCode(siteCode),
                sectorService.getBySiteCode(siteCode).stream()
                        .map(sector -> new SectorSummary(sector, antennaService.getBySectorName(sector.name()).stream()
                                .map(antenna -> new AntennaSummary(antenna, electricalTiltService.getByAntennaId(antenna.id())))
                                .toList()))
                        .toList());
    }
}
