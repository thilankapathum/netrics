package dev.thilanka.netrics.dto;

import jakarta.persistence.Column;

public record LteFddStandardKpiDto(
        String kpiName,
        String label,
        String unit,
        String type,
        String worstOrder,
        Double threshold
        ) {
}
