package dev.thilanka.netrics.dto;

import java.time.LocalDateTime;

public interface AnomalyCellsProjection {
    LocalDateTime getTimestamp();

    String getCellName();

    String getKpiName();

    String getKpiLabel();

    String getUnit();

    Double getValue();

    Double getPreviousValue();

    Double getDifference();

    Integer getImproved();

    String getSeverity();

    Boolean getHasAlarmCorrelation();

    Integer getDistinctAlarmDefCount();

    Integer getTotalAlarmOccurrences();

    String getBestMatchLevel();

}
