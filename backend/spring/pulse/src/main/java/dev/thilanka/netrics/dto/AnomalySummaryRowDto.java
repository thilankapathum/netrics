package dev.thilanka.netrics.dto;

public record AnomalySummaryRowDto(
        String kpiName,
        String kpiLabel,
        long critical,
        long high,
        long moderate,
        long total
) {
}
