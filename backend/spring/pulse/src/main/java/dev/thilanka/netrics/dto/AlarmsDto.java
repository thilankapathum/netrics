package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

public record AlarmsDto(
        Long alarmId,
        String nodeName,
        String severity,
        Timestamp occurrenceTime,
        String alarmType,
        Long alarmCode,
        String alarmName,
        String location,
        String ackState,
        String clearState,
        String specificProblem,
        String additionalInfo,
        String alarmSource
) {
}
