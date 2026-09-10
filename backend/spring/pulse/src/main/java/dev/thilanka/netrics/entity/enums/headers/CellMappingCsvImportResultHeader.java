package dev.thilanka.netrics.entity.enums.headers;

import lombok.Getter;

@Getter
public enum CellMappingCsvImportResultHeader {
    PREVIOUS_CELL_NAME("Previous Cell Name"),
    NEW_CELL_NAME("New Cell Name"),
    IMPORT_STATUS("Status"),
    ERROR_MESSAGE("Error Message");

    private final String header;

    CellMappingCsvImportResultHeader(String header) {
        this.header = header;
    }
}
