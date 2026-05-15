package dev.thilanka.beam.dto;


import dev.thilanka.beam.common.enums.CsvImportStatus;

public record SiteCsvImportResultDto(
        SiteDto siteDto,
        CsvImportStatus status,
        String errorMessage
) {
}
