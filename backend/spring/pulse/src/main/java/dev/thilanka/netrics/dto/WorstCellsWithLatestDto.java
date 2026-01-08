package dev.thilanka.netrics.dto;

public record WorstCellsWithLatestDto(
        Long id,
        String cellName,
        String kpiName,
        String kpiLabel,
        String unit,
        Double value,
        Double previousValue,
        Double difference,
        boolean improved,
        Double latestValue,
        Double latestDifference,
        boolean latestImproved
) {
}
