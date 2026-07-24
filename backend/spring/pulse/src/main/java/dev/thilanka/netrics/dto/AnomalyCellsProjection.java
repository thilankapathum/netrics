package dev.thilanka.netrics.dto;

public record AnomalyCellsProjection(
        String cellName,
        String kpiName,
        String kpiLabel,
        String unit,
        Double value,
        Double previousValue,
        Double difference,
        Integer improved,
        String severity,
        Boolean hasAlarmCorrelation,
        Integer distinctAlarmDefCount,
        Integer totalAlarmOccurrences,
        String bestMatchLevel
) {
}
