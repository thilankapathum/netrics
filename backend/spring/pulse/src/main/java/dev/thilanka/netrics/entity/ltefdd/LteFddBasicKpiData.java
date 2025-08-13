package dev.thilanka.netrics.entity.ltefdd;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LteFddBasicKpiData {
    private LteFddKpiDaySnap[] basicKpi;
    private List<LteFddKpiDaySnap[]> standardKpi = new ArrayList<>();
}
