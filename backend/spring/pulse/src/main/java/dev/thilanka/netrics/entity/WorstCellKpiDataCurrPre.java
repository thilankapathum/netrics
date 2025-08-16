package dev.thilanka.netrics.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorstCellKpiDataCurrPre {
    private String cellName;
    private String label;
    private Double kpiValue;
    private Double calculatedKpiValue;
    private Double preKpiValue;
    private Double preCalculatedKpiValue;
}
