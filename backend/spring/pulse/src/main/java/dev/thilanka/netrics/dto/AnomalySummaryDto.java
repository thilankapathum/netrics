package dev.thilanka.netrics.dto;

import java.util.List;

public record AnomalySummaryDto(
        List<AnomalySummaryRowDto> rows,
        AnomalySummaryRowDto grandTotal
) {
}
