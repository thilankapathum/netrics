package dev.thilanka.netrics.entity.common;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class OperationLogFilter {
    private String entityName;          // e.g. "Cell"
    private String entityId;            // filter to one specific row
    private OperationLog.OperationType operation; // CREATE | UPDATE | DELETE
    private String performedBy;         // user ID

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;
}
