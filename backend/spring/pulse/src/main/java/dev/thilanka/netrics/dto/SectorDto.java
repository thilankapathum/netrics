package dev.thilanka.netrics.dto;

public record SectorDto(
        Integer sectorIndex,
        String name,
        Integer azimuth,
        String siteCode
) {
}
