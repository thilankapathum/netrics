package dev.thilanka.netrics.entity;


import java.time.LocalDateTime;

public interface KpiData {
     LocalDateTime getTimestamp();
     String getCellName();
     String getKpiLabel();
     Double getKpiValue();
}
