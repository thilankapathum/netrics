package dev.thilanka.beam.dto;

public record AntennaTypeDto(
        Long id,
        String name,
        int portCount,
        int height,
        int width,
        boolean retAvailability
) {
}
