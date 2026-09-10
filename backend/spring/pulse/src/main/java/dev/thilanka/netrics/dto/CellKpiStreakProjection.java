package dev.thilanka.netrics.dto;

public record CellKpiStreakProjection(
    String cellName,
    Long standardKpiId,
    Integer consecutiveBadDays
){
}
