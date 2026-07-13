package dev.thilanka.netrics.dto;

public record WorstCellsProjection(
        String cellName,
        String kpiName,
        String kpiLabel,
        String unit,
        Double value,
        Double previousValue,
        Double difference,
        Integer improved
) {
}
