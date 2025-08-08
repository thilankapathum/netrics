package dev.thilanka.netrics.dto;

public record LteFddKpiMappingDto(
        String ossKpiName,
        Double multiplicationFactor,  //-- Setting default multiplication factor
        String ossIdentifier,
        String lteFddStandardKpi
        ) {
}
