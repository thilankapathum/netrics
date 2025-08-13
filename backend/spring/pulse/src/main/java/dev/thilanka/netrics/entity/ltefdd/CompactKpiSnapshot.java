package dev.thilanka.netrics.entity.ltefdd;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompactKpiSnapshot {
    private String kpiLabel;
    private boolean basic;
    private Double value;
    private Double difference;
    private boolean up;
}
