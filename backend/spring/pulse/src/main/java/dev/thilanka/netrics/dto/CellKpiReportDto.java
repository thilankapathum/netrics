package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

public record CellKpiReportDto(
        Timestamp timestamp,
        String siteCode,
        String cellName,
        String bandName,
        String kpiName,
        Double kpiValue
) {
}
