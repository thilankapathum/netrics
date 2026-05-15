package dev.thilanka.beam.common.enums.headers;

import lombok.Getter;

@Getter
public enum SectorCsvHeader {
    SITE_CODE("Site ID"),
    SECTOR_NAME("Sector Name"),
    SECTOR_INDEX("Sector Index"),
    AZIMUTH("Azimuth");

    private final String header;
    SectorCsvHeader(String header) {
        this.header = header;
    }
}
