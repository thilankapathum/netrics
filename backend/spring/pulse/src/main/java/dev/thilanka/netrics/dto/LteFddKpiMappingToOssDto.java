package dev.thilanka.netrics.dto;

public record LteFddKpiMappingToOssDto(
        String ossKpiName,
        Double multiplicationFactor,  //-- Setting default multiplication factor
        String ossIdentifier,
        String lteFddStandardKpi
        ) {
}
