package dev.thilanka.netrics.dto;

public record StandardKpiDto(
        String kpiName,
        String label,
        String unit,
        String type,
        String worstOrder,
        Double threshold,
        String aggregation,
        String basicKpi,
        String ratName
        ) {
}
