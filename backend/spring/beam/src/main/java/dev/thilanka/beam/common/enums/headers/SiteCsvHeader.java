package dev.thilanka.beam.common.enums.headers;

import lombok.Getter;

@Getter
public enum SiteCsvHeader {
    SITE_CODE("Site ID"),
    SITE_NAME("Site Name"),
    LATITUDE("Latitude"),
    LONGITUDE("Longitude"),
    BUILDING_HEIGHT("buildingHeight"),
    TOWER_HEIGHT("towerHeight"),
    OPERATOR_NAME("operatorName"),
    INFRA_TYPE_NAME("infraType");

    private final String header;
    SiteCsvHeader(String header) {
        this.header = header;
    }

}
