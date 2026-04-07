package dev.thilanka.netrics.entity.enums;

import lombok.Getter;

@Getter
public enum SiteCsvHeader {
    SITE_CODE("Site ID"),
    SITE_NAME("Site Name"),
    LATITUDE("Latitude"),
    LONGITUDE("Longitude");

    private final String header;
    SiteCsvHeader(String header) {
        this.header = header;
    }

}
