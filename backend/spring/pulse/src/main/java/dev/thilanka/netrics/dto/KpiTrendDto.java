package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

public record KpiTrendDto(
        Timestamp timestamp,
        String kpiLabel,
        Double kpiValue
) {
}
