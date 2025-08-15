package dev.thilanka.netrics.dto;

public record LteFddBasicKpiDto(
        String kpiName,
        String label,
        String worstOrder,
        Double threshold) {
}
