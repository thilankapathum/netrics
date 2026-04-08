package dev.thilanka.netrics.entity.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum CellCsvHeader {

    CELL_NAME("Cell Name"),
    SITE_CODE("Site ID"),
    NODE_NAME("Node Name"),
    RAT_NAME("RAT"),
    BAND_NAME("Band"),
    AZIMUTH("Azimuth"),
    BEAMWIDTH("Beamwidth"),
    IS_MULTI_BEAM("Multi-Beam Cell"),
    CARRIER_NAME("Carrier Name"),
    SECTOR_NAME("Sector Name");

    private final String header;

    CellCsvHeader(String header){
        this.header = header;
    }
}
