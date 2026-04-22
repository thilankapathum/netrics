package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.MapCell;

import java.util.List;

public interface MapCellService {
    List<MapCell> getMapCellsByKpi(Double minLng, Double minLat, Double maxLng, Double maxLat ,String standardKpiName, String ratName, String granularityName);
}
