package dev.thilanka.netrics.dto;

import java.util.List;

public record SectorUpdateResult(
        SectorDto sectorDto,
        List<String> warnings
) {
}
