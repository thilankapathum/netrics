package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public record MapCellThrSetDto(
        String standardKpiName,
        String granularityName,
        String ratName,
        String userId,
        boolean isAdmin,
        boolean isActive,
        boolean isDeleted,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}