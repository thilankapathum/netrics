package dev.thilanka.netrics.dto;

public record CellAlarmCorrelationProjection(
        String cellName,
        Boolean hasAlarmCorrelation,
        Integer distinctAlarmDefCount,
        Integer totalAlarmOccurrences,
        String bestMatchLevel
) {
}
