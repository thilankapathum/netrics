package dev.thilanka.netrics.dto;
import java.time.LocalDateTime;

public record KpiDataDto(
        LocalDateTime timestamp,
        String cellName,
        String kpiLabel,
        Double kpiValue
) {
}
