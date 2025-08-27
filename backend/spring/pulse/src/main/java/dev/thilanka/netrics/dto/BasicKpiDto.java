package dev.thilanka.netrics.dto;

public record BasicKpiDto(
        String kpiName,
        String label,
        String worstOrder,
        Double threshold,
        String aggregation,
        String unit) {
}
