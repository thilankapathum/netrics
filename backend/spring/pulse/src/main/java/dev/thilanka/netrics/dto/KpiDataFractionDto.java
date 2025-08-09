package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public record KpiDataFractionDto(
        LocalDateTime timestamp,
        String cellName,
        String lteFddStandardKpi,
        String kpiValue,
        String numerator,
        String denominator
) {
}
