package dev.thilanka.netrics.entity.ltefdd;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiSnapshot {
//    private Timestamp timestamp;
    private String label;
    private Double kpiValueSum = 0.0;
    private Double numeratorKpiValueSum = 0.0;
    private Double denominatorKpiValueSum = 0.0;
}
