package dev.thilanka.netrics.dto;

//-- To display Worst-cells in Frontend Worst-cell dashboard

import java.time.LocalDateTime;

public record WorstCellsDashboardDto(
        Long id,
        String cellName,
        String kpiName,
        String kpiLabel,
        String unit,
        Double value,
        Double previousValue,
        Double difference,
        boolean improved
) {
}
