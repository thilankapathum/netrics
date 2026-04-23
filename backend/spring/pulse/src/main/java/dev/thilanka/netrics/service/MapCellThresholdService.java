package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.MapCellThrSetAndThresholds;
import dev.thilanka.netrics.dto.MapCellThresholdDto;
import dev.thilanka.netrics.entity.MapCellThreshold;

import java.util.List;

public interface MapCellThresholdService {

    MapCellThreshold createThreshold(MapCellThreshold threshold);

    MapCellThresholdDto createThreshold(MapCellThresholdDto dto);

    List<MapCellThreshold> findThresholdsByThrSetId(Long id);

    List<MapCellThresholdDto> getThresholdsByThrSetId(Long id);

    MapCellThrSetAndThresholds getThrSetAndThresholdsByThrSetId(Long id);
}
