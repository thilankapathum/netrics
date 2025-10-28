package dev.thilanka.netrics.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record DateRangeDto(
        Timestamp currentDate,
        Timestamp previousDate
//        LocalDateTime currentDate,
//        LocalDateTime previousDate
) {
}
