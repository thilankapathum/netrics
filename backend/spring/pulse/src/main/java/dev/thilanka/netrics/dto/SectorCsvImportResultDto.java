package dev.thilanka.netrics.dto;

import dev.thilanka.netrics.entity.enums.CsvImportStatus;

public record SectorCsvImportResultDto(
        SectorDto sectorDto,
        CsvImportStatus status,
        String errorMessage
) {
}
