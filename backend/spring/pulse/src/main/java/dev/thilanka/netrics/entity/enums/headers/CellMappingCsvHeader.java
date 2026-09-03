package dev.thilanka.netrics.entity.enums.headers;

import lombok.Getter;

@Getter
public enum CellMappingCsvHeader {
    PREVIOUS_CELL_NAME("Previous Cell Name"),
    NEW_CELL_NAME("New Cell Name");

    private final String header;

    CellMappingCsvHeader(String header) {
        this.header = header;
    }
}
