package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BoundingBox;
import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.repository.MapCellRepository;
import dev.thilanka.netrics.service.*;
import dev.thilanka.netrics.util.TileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    private final TileUtils tileUtils;


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

    @Override
    @Cacheable(value = "mapCellTiles",
            key = "#z + '_' + #x + '_' + #y + '_' + #standardKpiName + '_' + #date + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public List<MapCell> getMapCellsByTile(int z, int x, int y, String standardKpiName, String ratName, String granularityName, String date, String areaName) {
        BoundingBox bbox = tileUtils.tileToBoundingBox(x, y, z);

        double bufferDegrees = 0.005;

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        LocalDateTime startTime = dateService.extractDate(date);
        LocalDateTime endTime = startTime.plusSeconds(granularity.getPlusSeconds());
        Area area = areaService.findAreaByName(areaName);

        return mapCellRepository.queryCellsByKpiTile(bbox.minLng(), bbox.minLat(), bbox.maxLng(), bbox.maxLat(),
                bufferDegrees, standardKpi.getId(), rat.getId(), granularity.getId(), startTime, endTime, area.getId());
    }

    @Override

    public byte[] getTile(int z, int x, int y, Long standardKpiId, Long ratId, Long granularityId, Long areaId, LocalDateTime startTime, LocalDateTime endTime) {
        return mapCellRepository.getTile(z, x, y, standardKpiId, ratId, granularityId, startTime, endTime, areaId);
    }

    @Override
    @Cacheable(value = "mapCellTiles",
            key = "#z + '_' + #x + '_' + #y + '_' + #standardKpiName + '_' + #areaName + '_' + #date + '_' + #granularityName + '_' + #ratName")
    public byte[] getTile(int z, int x, int y, String standardKpiName, String ratName, String granularityName, String date, String areaName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        LocalDateTime startTime = dateService.extractDate(date);
        LocalDateTime endTime = startTime.plusSeconds(granularity.getPlusSeconds());
        Area area = areaService.findAreaByName(areaName);

        return getTile(z, x, y, standardKpi.getId(), rat.getId(), granularity.getId(), area.getId(), startTime, endTime);
    }
}
