package dev.thilanka.netrics.dto;

public record AlarmFilter(
        String period,
        String nodeName,
        String severity,
        String alarmType,
        String alarmName,
        String ackState,
        String clearState,
        String alarmSource,
        String areaName
) {
}
