package dev.thilanka.netrics.entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorstCell {
    private String cellName;
    private String kpiLabel;
    private Double value;
    private Double previousValue;
    private Double difference;
    private boolean improved;
    private String unit;
}
