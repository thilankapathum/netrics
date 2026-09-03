package dev.thilanka.netrics.dto;

import dev.thilanka.netrics.entity.enums.CsvImportStatus;

public record CellMappingCsvImportResultDto(
        CellMappingDto cellMappingDto,
        CsvImportStatus status,
        String errorMessage
) {
}
