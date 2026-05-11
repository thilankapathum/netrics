package dev.thilanka.netrics.dto;

public record CellDto(
        String cellName,
        String nodeName,
        String ratName,
        String siteCode,
        String bandName,
        Integer azimuth,
        Integer beamwidth,
        Boolean isMultiBeam,
        String carrierName,
        String sectorName
) {
}
