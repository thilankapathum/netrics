package dev.thilanka.netrics.entity.ltefdd;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LteFddKpiDaySnap {
//    private Timestamp timestamp;
    private Long lteFddStandardKpiId;
    private Double kpiValueSum;
    private Double numeratorKpiValueSum;
    private Double denominatorKpiValueSum;
}
