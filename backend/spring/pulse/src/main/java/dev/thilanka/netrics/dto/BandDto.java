package dev.thilanka.netrics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BandDto(
        @NotBlank String name,
        @Min(1) int number,
        @NotBlank String unit
) {
}
