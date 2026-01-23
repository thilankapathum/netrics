package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.SiteKpiReportDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiReportServiceImpl implements KpiReportService {
    private final KpiDayRepository kpiDayRepository;
    private final RatService ratService;
    private final GranularityService granularityService;
    private final StandardKpiService standardKpiService;
    private final AreaService areaService;
    private final DateService dateService;

    @Override
    public List<SiteKpiReportDto> getSiteWiseReportByKpiAndDate(String ratName, String granularityName, String standardKpiName, String startDate, String endDate, String areaName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName,rat);
        LocalDateTime startTimestamp = dateService.extractDate(startDate).toLocalDate().atStartOfDay();
        LocalDateTime endTimestamp = dateService.extractDate(endDate).toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
        Area area = areaService.findAreaByName(areaName);

        return kpiDayRepository.getSiteWiseReportByKpiAndDate(rat.getId(), granularity.getId(), standardKpi.getId(), startTimestamp,endTimestamp, area.getId());
    }
}
