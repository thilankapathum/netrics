package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public record WorstCellsDto(
        LocalDateTime timestamp,
        String cellName,
        String kpiName,
        String kpiLabel,
        String unit,
        Double value,
        Double previousValue,
        Double difference,
        Integer improved,
        Integer consecutiveBadDays,
        String severity,
        Boolean hasAlarmCorrelation,
        Integer distinctAlarmDefCount,
        Integer totalAlarmOccurrences,
        String bestMatchLevel
) {
}
