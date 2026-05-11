package dev.thilanka.netrics.dto;

public record SiteEvent(
        String eventType,   // "CREATED", "UPDATED", "DELETED"
        Long id,
        String siteCode,
        String siteName,
        Double latitude,
        Double longitude
) {
}
