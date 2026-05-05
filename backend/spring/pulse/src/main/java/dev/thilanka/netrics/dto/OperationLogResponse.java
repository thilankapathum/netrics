package dev.thilanka.netrics.dto;

import dev.thilanka.netrics.entity.common.OperationLog;

import java.time.LocalDateTime;
import java.util.Map;

public record OperationLogResponse(
        Long id,
        String entityName,
        String entityId,
        OperationLog.OperationType operation,
        LocalDateTime performedAt,
        String performedBy,
        Map<String, Map<String, Object>> changes
        ) {
}
