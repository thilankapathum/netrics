package dev.thilanka.netrics.dto;

public record MapCell(
        String siteCode,
        String siteName,
        String cellName,
        Double latitude,
        Double longitude,
        Integer azimuth,
        Integer beamwidth,
        String kpiLabel,
        Double kpiValue,
        Integer radius
) {
}
