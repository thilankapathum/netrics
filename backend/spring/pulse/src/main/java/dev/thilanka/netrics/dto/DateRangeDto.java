package dev.thilanka.netrics.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record DateRangeDto(
        LocalDateTime currentDate,
        LocalDateTime previousDate
) {
}
