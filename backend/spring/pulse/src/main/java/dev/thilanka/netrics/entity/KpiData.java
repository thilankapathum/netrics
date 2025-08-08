package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KpiData {
    private LocalDateTime timestamp;
    private String cellName;
    private String siteName;
    private LteFddStandardKpi lteFddStandardKpi;
    private String kpiValue;
}
