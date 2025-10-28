package dev.thilanka.netrics.dto;

public record StandardRawKpiMappingDto(
        String standardKpi,
        String numerator,
        String denominator,
        String ratName
) {
}
