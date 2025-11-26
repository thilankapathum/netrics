package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

//-- For Querying worst-cells from KPI-day repository and save in worst_cells (WorstCell) table

public record WorstCellSaveDto(
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
