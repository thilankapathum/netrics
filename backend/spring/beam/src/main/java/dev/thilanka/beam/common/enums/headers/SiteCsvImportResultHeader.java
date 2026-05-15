package dev.thilanka.beam.common.enums.headers;

import lombok.Getter;

@Getter
public enum SiteCsvImportResultHeader {
    SITE_CODE("Site ID"),
    SITE_NAME("Site Name"),
    LATITUDE("Latitude"),
    LONGITUDE("Longitude"),
    BUILDING_HEIGHT("buildingHeight"),
    TOWER_HEIGHT("towerHeight"),
    OPERATOR_NAME("operatorName"),
    INFRA_TYPE_NAME("infraType"),
    IMPORT_STATUS("Status"),
    ERROR_MESSAGE("Error Message");

    private final String header;

    SiteCsvImportResultHeader(String header) {
        this.header = header;
    }
}
