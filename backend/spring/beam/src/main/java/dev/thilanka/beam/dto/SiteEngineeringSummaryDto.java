package dev.thilanka.beam.dto;

import java.util.List;

public record SiteEngineeringSummaryDto(
        SiteDto site,
        List<SectorSummary> sectors
) {
    public record SectorSummary(SectorDto sector, List<AntennaSummary> antennas) {
    }

    public record AntennaSummary(AntennaDto antenna, List<ElectricalTiltDto> electricalTilts) {
    }
}
