package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

public record KpiDataWithOperandsDto(
        Timestamp timestamp,
        String cellName,
        String kpiLabel,
        Double kpiValue,
        Double numeratorKpiValue,
        Double denominatorKpiValue
) {
}
