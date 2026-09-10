package dev.thilanka.netrics.dto;

public record CellMappingDto(
        Long id,
        String previousCellName,
        String newCellName
) {
}
