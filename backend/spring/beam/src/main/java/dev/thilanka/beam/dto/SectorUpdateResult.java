package dev.thilanka.beam.dto;

import java.util.List;

public record SectorUpdateResult(
        SectorDto sectorDto,
        List<String> warnings
) {
}
