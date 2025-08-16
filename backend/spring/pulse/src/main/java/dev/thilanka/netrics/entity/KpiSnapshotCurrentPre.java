package dev.thilanka.netrics.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiSnapshotCurrentPre {
    private String label;
    private String worstOrder;
    private Double kpiValue;
    private Double calculatedKpiValue;
    private Double preKpiValue;
    private Double preCalculatedKpiValue;
}
