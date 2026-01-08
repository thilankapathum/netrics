package dev.thilanka.netrics.dto;

public record GranularityDto(
        String name,
        String label,
        int plusSeconds
) {
}
