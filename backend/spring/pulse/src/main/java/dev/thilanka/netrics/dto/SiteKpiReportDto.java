package dev.thilanka.netrics.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record SiteKpiReportDto(
        Timestamp timestamp,
        String siteCode,
        String kpiName,
        String label,
        Double kpiValue,
        String concatBands
) {
}
