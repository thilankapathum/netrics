package dev.thilanka.netrics.dto;

import dev.thilanka.netrics.entity.enums.CsvImportStatus;

public record SiteCsvImportResultDto(
        SiteDto siteDto,
        CsvImportStatus status,
        String errorMessage
) {
}
