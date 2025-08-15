package dev.thilanka.netrics.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalKpiSnapshot {
    private String kpiLabel;
    private boolean isBasic;
    private Double value;
    private Double previousValue;
    private Double difference;
    private boolean improved;
}
