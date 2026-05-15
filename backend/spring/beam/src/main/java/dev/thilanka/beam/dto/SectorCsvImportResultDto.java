package dev.thilanka.beam.dto;


import dev.thilanka.beam.common.enums.CsvImportStatus;

public record SectorCsvImportResultDto(
        SectorDto sectorDto,
        CsvImportStatus status,
        String errorMessage
) {
}
