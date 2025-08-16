package dev.thilanka.netrics.dto;

public record LteFddStandardKpiDto(
        String kpiName,
        String label,
        String unit,
        String type,
        String worstOrder,
        Double threshold,
        String aggregation
        ) {
}
