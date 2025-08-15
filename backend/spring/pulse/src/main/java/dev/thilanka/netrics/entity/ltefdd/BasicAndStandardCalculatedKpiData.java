package dev.thilanka.netrics.entity.ltefdd;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicAndStandardCalculatedKpiData {
    private CalculatedKpiSnapshot[] basicKpi;
    private List<CalculatedKpiSnapshot[]> standardKpis = new ArrayList<>();
}
