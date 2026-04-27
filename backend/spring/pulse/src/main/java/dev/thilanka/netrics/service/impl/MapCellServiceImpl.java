package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.repository.MapCellRepository;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapCellServiceImpl implements MapCellService {
    private final MapCellRepository mapCellRepository;
    private final StandardKpiService standardKpiService;
    private final RatService ratService;
    private final GranularityService granularityService;
    private final DateService dateService;
    private final AreaService areaService;


    @Override
    public List<MapCell> getMapCellsByKpi(Double minLng, Double minLat, Double maxLng, Double maxLat, String standardKpiName, String ratName, String granularityName, String date, String areaName) {

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        LocalDateTime startTime = dateService.extractDate(date);
        LocalDateTime endTime = startTime.plusSeconds(granularity.getPlusSeconds());
        Area area = areaService.findAreaByName(areaName);


        return mapCellRepository.queryCellsByKpi(minLng, minLat, maxLng, maxLat, standardKpi.getId(), rat.getId(), granularity.getId(), startTime, endTime, area.getId());
    }
}
