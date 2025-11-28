package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public record WorstCellCommentDto(
        Long id,
        String comment,
        Long worstCellId,
        LocalDateTime createdAt,
        LocalDateTime lastModifiedAt,
        String createdBy,
        String lastModifiedBy
) {
}
