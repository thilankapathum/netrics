package dev.thilanka.netrics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Sets the same azimuth on several cells, e.g. to follow their sector */
public record CellAzimuthUpdateDto(
        @NotEmpty List<String> cellNames,
        @NotNull @Min(0) @Max(360) Integer azimuth
) {
}
