package dev.thilanka.netrics.dto;

import java.util.List;

public record BasicKpiWithStandardKpiDto(
        String kpiName,
        String label,
        String worstOrder,
        Double threshold,
        String aggregation,
        String unit,
        String ratName,
        List<StandardKpiDto> standardKpis
) {
}
