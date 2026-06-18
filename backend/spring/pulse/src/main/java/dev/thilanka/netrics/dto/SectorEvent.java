package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

//-- DO NOT MODIFY WITHOUT BEAM'S SECTOR-EVENT
public record SectorEvent(
        String eventType,   // "CREATED", "UPDATED", "DELETED"
//        Long id,
        Integer sectorIndex,
        String name,
        Integer azimuth,
//        Long siteId,
        String siteCode,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy
) {
}