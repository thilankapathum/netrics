package dev.thilanka.netrics.entity.enums;

import lombok.Getter;

@Getter
public enum CellCsvImportResultHeader {
    CELL_NAME("Cell Name"),
    SITE_CODE("Site ID"),
    NODE_NAME("Node Name"),
    RAT_NAME("RAT"),
    BAND_NAME("Band"),
    IMPORT_STATUS("Status"),
    ERROR_MESSAGE("Error Message");

    private final String header;

    CellCsvImportResultHeader(String header){
        this.header = header;
    }
}
