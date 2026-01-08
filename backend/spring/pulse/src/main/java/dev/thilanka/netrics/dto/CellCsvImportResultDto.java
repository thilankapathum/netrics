package dev.thilanka.netrics.dto;

import dev.thilanka.netrics.entity.enums.CsvImportStatus;

public record CellCsvImportResultDto(
        CellDto cellDto,
        CsvImportStatus status,
        String errorMessage
) {
}
