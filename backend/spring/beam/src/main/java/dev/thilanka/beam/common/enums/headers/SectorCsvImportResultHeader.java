package dev.thilanka.beam.common.enums.headers;

import lombok.Getter;

@Getter
public enum SectorCsvImportResultHeader {
    SITE_CODE("Site ID"),
    SECTOR_NAME("Sector Name"),
    SECTOR_INDEX("Sector Index"),
    AZIMUTH("Azimuth"),
    IMPORT_STATUS("Status"),
    ERROR_MESSAGE("Error Message");

    private final String header;
    SectorCsvImportResultHeader(String header) {
        this.header = header;
    }
}
