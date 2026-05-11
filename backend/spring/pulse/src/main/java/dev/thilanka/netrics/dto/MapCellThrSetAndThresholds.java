package dev.thilanka.netrics.dto;

import java.util.List;

public record MapCellThrSetAndThresholds(
        MapCellThrSetResponseDto thrSet,
        List<MapCellThresholdDto> thresholds
) {
}
