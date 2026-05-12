package dev.thilanka.beam.dto;

import java.time.LocalDateTime;

//-- DO NOT MODIFY WITHOUT PULSE'S BAND-EVENT
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