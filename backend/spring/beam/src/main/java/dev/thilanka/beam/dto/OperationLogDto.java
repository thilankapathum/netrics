package dev.thilanka.beam.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.time.LocalDateTime;

/**
 * One engineering-parameter change. {@code siteCode}, {@code sectorName} and {@code antennaIndex} locate the
 * changed row and are null when it (or its parent) has since been deleted.
 */
public record OperationLogDto(
        Long id,
        String entityName,
        String entityId,
        String operation,
        LocalDateTime performedAt,
        String performedBy,
        /* field -> {old, new}, passed through as stored */
        @JsonRawValue String changes,
        String siteCode,
        String sectorName,
        Integer antennaIndex
) {
}
