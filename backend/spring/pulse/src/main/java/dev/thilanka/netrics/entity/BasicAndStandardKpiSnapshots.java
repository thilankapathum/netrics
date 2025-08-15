package dev.thilanka.netrics.entity;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicAndStandardKpiSnapshots {
    private KpiSnapshot[] basicKpi;
    private List<KpiSnapshot[]> standardKpis = new ArrayList<>();
}
