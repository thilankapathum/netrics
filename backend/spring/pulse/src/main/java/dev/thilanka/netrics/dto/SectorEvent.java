package dev.thilanka.netrics.dto;

public record SectorEvent(
        String eventType,   // "CREATED", "UPDATED", "DELETED"
        Long id,
        Integer sectorIndex,
        String name,
        Integer azimuth,
        Long siteId
) {
}