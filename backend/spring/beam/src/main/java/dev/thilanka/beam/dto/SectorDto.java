package dev.thilanka.beam.dto;

public record SectorDto(
        Integer sectorIndex,
        String name,
        Integer azimuth,
        String siteCode
) {
}
