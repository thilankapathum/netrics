package dev.thilanka.beam.dto;

import java.time.LocalDateTime;

//-- DO NOT MODIFY WITHOUT PULSE'S SITE-EVENT
public record SiteEvent(
        String eventType,   // "CREATED", "UPDATED", "DELETED"
//        Long id,
        String siteCode,
        String siteName,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String createdBy,
        String modifiedBy
) {
}
