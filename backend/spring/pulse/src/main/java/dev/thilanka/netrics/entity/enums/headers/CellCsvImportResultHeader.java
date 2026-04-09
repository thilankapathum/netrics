package dev.thilanka.netrics.entity.enums.headers;

import lombok.Getter;

@Getter
public enum CellCsvImportResultHeader {
    CELL_NAME("Cell Name"),
    SITE_CODE("Site ID"),
    NODE_NAME("Node Name"),
    RAT_NAME("RAT"),
    BAND_NAME("Band"),
    AZIMUTH("Azimuth"),
    BEAMWIDTH("Beamwidth"),
    IS_MULTI_BEAM("Multi-Beam Cell"),
    CARRIER_NAME("Carrier Name"),
    SECTOR_NAME("Sector Name"),
    IMPORT_STATUS("Status"),
    ERROR_MESSAGE("Error Message");

    private final String header;

    CellCsvImportResultHeader(String header){
        this.header = header;
    }
}
