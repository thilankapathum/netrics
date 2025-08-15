package dev.thilanka.netrics.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiSnapshot {
    private String label;
    private String worstOrder;
    private Double kpiValueSum = 0.0;
    private Double calculatedKpiValue = 0.0;
}
