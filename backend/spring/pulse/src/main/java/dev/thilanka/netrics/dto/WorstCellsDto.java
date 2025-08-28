package dev.thilanka.netrics.dto;

public record WorstCellsDto(
        String cellName,
         String kpiLabel,
         String unit,
         Double value,
         Double previousValue,
         Double difference,
         Integer improved
) {
}
