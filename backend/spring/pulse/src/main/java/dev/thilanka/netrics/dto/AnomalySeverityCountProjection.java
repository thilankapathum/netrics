package dev.thilanka.netrics.dto;

public record AnomalySeverityCountProjection(
        String kpiName,
        String kpiLabel,
        String severity,
        Long cnt
) {
}
