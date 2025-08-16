package dev.thilanka.netrics.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorstCellKpiData {
    private String cellName;
    private String label;
    private Double kpiValue;
    private Double calculatedKpiValue;
}
