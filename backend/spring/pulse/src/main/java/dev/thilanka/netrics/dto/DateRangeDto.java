package dev.thilanka.netrics.dto;

import java.sql.Timestamp;

public record DateRangeDto(
        Timestamp currentDate,
        Timestamp previousDate
) {
}
