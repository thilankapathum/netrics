package dev.thilanka.netrics.dto;

public record CellDto(
        String cellName,
        String nodeName,
        String ratName,
        String siteCode,
        String bandName,
        Integer azimuth,
        Integer beamwidth,
        boolean isMultiBeam,
        String carrierName,
        String sectorName
) {
}
