package dev.thilanka.netrics.dto;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public record KpiDataDto(
//        LocalDateTime timestamp,
        Timestamp timestamp,
        String cellName,
        String kpiLabel,
        Double kpiValue
) {
}
