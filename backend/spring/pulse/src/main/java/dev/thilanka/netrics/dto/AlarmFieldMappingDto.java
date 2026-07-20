package dev.thilanka.netrics.dto;

public record AlarmFieldMappingDto(
        String canonicalField,
        String sourceColumn,
        String extractionRegex,
        String defaultValue,
        boolean isRequired
) {
}
