package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public interface KpiAnomalyProjection {
    String getCellName();
    String getKpiName();
    String getKpiLabel();
    String getUnit();
    LocalDateTime getTimestamp();
    Double getObservedValue();
    Double getBaselineMedian();
    Double getMad();
    Double getRobustZScore();
    String getSeverity();
}
