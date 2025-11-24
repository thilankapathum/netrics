package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

public record DashboardWorstCellDto(
        Timestamp timestamps,
        String cellName,
        Long standardKpiId,
        String unit,
        Double value,
        Double previousValue,
        Double difference,
        Integer improved,
        Long ratId
) {
}
