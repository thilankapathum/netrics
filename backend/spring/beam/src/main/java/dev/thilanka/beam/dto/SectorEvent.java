package dev.thilanka.beam.dto;

import java.time.LocalDateTime;

//-- DO NOT MODIFY WITHOUT PULSE'S SECTOR-EVENT
public record SectorEvent(
        String eventType,   // "CREATED", "UPDATED", "DELETED"
        Long id,
        Integer sectorIndex,
        String name,
        Integer azimuth,
        Long siteId,
        LocalDateTime createdAt,
        String createdBy
) {
}