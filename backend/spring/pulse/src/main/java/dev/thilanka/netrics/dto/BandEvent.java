package dev.thilanka.netrics.dto;

import jakarta.persistence.Column;

import java.time.LocalDateTime;

public record BandEvent(
        String eventType,   // "CREATED", "UPDATED", "DELETED"
        Long id,
        String name,
        int number,
        String unit,
        LocalDateTime createdAt,
        String createdBy
) {
}