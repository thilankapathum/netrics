package dev.thilanka.netrics.entity.enums;

import lombok.Getter;

@Getter
public enum SiteCsvImportResultHeader {
    SITE_CODE("Site ID"),
    SITE_NAME("Site Name"),
    LATITUDE("Latitude"),
    LONGITUDE("Longitude"),
    IMPORT_STATUS("Status"),
    ERROR_MESSAGE("Error Message");

    private final String header;

    SiteCsvImportResultHeader(String header) {
        this.header = header;
    }
}
