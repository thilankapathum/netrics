package dev.thilanka.netrics.dto;

public record BoundingBox(
        double minLng,
        double minLat,
        double maxLng,
        double maxLat
) {
}
