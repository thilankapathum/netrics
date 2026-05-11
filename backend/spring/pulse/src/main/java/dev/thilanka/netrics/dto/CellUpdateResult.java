package dev.thilanka.netrics.dto;

import java.util.List;

public record CellUpdateResult(
        CellDto cellDto,
        List<String> warnings
) {
}
