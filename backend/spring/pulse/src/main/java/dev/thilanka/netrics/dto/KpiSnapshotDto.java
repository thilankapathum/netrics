package dev.thilanka.netrics.dto;

public record KpiSnapshotDto(
        String kpiLabel,
         String unit,
         Double value,
         Double previousValue,
         Double difference,
         Long improved
) {
}
