package dev.thilanka.netrics.dto;

public record MapCellThresholdDto(
        Double minValue,
        Double maxValue,
        String color,
        String label,
        Integer priority,
        Long mapCellThrSetId

) {
}