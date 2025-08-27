package dev.thilanka.netrics.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicKpiSnapshot {
    private String kpiLabel;
    private Double value;
    private Double previousValue;
    private Double difference;
    private boolean improved;
    private String unit;
    private List<KpiSnapshot> standardKpis;
}
