package dev.thilanka.netrics.entity.ltefdd;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicStandardKpiData {
    private KpiSnapshot[] basicKpi;
    private List<KpiSnapshot[]> standardKpi = new ArrayList<>();
}
