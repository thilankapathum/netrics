package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public record KpiAnomalyDto(
        String cellName,
        String kpiName,
        String kpiLabel,
        String unit,
        LocalDateTime timestamp,
        Double observedValue,
        Double baselineMedian,
        Double mad,
        Double robustZScore,
        String severity
) {
}
