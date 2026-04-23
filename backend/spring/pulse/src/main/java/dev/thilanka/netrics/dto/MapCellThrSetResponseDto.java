package dev.thilanka.netrics.dto;

public record MapCellThrSetResponseDto(
        Long id,
        String standardKpiName,
        String granularityName,
        String ratName,
        boolean isAdmin
) {
}
