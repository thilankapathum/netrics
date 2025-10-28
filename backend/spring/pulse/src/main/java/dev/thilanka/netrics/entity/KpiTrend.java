package dev.thilanka.netrics.entity;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiTrend {
    private Timestamp timestamp;
    private String kpiLabel;
    private Double kpiValue;
}
