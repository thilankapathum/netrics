package dev.thilanka.netrics.dto;

public record KpiMappingToOssDto(
        String ossKpiName,
        Double multiplicationFactor,  //-- Setting default multiplication factor
        String ossIdentifier,
        String standardKpi,
        String ratName
        ) {
}
