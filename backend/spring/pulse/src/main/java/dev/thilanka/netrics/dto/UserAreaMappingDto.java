package dev.thilanka.netrics.dto;

public record UserAreaMappingDto(
        Long id,
        String userId,
        String userFullName,
        String areaTypeName,
        String areaName
) {
}
