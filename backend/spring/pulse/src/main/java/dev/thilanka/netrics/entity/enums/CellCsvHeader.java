package dev.thilanka.netrics.entity.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum CellCsvHeader {

    CELL_NAME("Cell Name"),
    SITE_CODE("Site ID"),
    NODE_NAME("Node Name"),
    RAT_NAME("RAT"),
    BAND_NAME("Band");

    private final String header;

    CellCsvHeader(String header){
        this.header = header;
    }
}
