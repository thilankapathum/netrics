package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public record KpiDayDto(
        LocalDateTime timestamp,
        String cellName,
        String siteName,

        Double kpiValue,
        Double numeratorKpiValue,
        Double denominatorKpiValue,

        String dataType,
        String fileName,

        Long standardKpi,
        Long numeratorKpi,
        Long denominatorKpi,

        Long oss,
        Long districtCode,
        Long granularityId
) {
}
