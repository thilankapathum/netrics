package dev.thilanka.netrics.dto;

public record WorstCellCreationStatusDto(
        boolean status,
        int totalItems,
        int executedItems
) {
}
