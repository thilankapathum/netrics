package dev.thilanka.netrics.dto;

public record CellStreakProjection(
    String cellName,
    Integer consecutiveBadDays
){
}
