package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.MapCell;
import dev.thilanka.netrics.dto.SiteDto;

import java.time.LocalDateTime;
import java.util.List;

public interface MapCellService {
    List<MapCell> getMapCellsByKpi(Double minLng, Double minLat, Double maxLng, Double maxLat ,String standardKpiName, String ratName, String granularityName, String date, String areaName);

    List<MapCell> getMapCellsByTile(int z, int x, int y,
                                    String standardKpiName, String ratName,
                                    String granularityName, String date, String areaName);

    List<MapCell> getMapCellsByTileAndBand(int z, int x, int y,
                                           String standardKpiName, String ratName,
                                           String granularityName, String date, String areaName, String bandName);

    List<SiteDto> getSitesByTile(int z, int x, int y);

    byte[] getTile(int z, int x, int y,
                   Long standardKpiId,
                   Long ratId,
                   Long granularityId,
                   Long areaId,
                   LocalDateTime startTime,
                   LocalDateTime endTime);

    byte[] getTile(int z, int x, int y, String standardKpiName, String ratName, String granularityName, String date, String areaName);
}
