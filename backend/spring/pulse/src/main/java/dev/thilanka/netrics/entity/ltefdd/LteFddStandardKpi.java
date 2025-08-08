package dev.thilanka.netrics.entity.ltefdd;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lte_fdd_standard_kpi")
public class LteFddStandardKpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String kpiName;
    @Column(nullable = false)
    private String unit;
    @Column(nullable = false)
    private String type;
    private String worstOrder;
    private Double threshold;

    @OneToMany(mappedBy = "lteFddStandardKpi",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiMapping> lteFddKpiMappings;

    @OneToMany(mappedBy = "lteFddStandardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiDay> lteFddKpiDays;
}
