package dev.thilanka.netrics.entity.ltefdd;

import dev.thilanka.netrics.entity.Rat;
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
    @Column(unique = true, nullable = false)
    private String label;
    @Column(nullable = false)
    private String unit;
    @Column(nullable = false)
    private String type;
    private String worstOrder;
    private Double threshold;
    private String aggregation;

    @OneToMany(mappedBy = "lteFddStandardKpi",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiMappingToOss> lteFddKpiMappingToOsses;

    @OneToMany(mappedBy = "lteFddStandardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiDay> lteFddKpiDays;

    @OneToMany(mappedBy = "numeratorKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiDay> lteFddKpiDaysNumerator;

    @OneToMany(mappedBy = "denominatorKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiDay> lteFddKpiDaysDenominator;


    @OneToOne(mappedBy = "standardKpi",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private LteFddStandardRawKpiMapping standardKpi;

    @OneToOne(mappedBy = "numerator", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private  LteFddStandardRawKpiMapping numerator;

    @OneToOne(mappedBy = "denominator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private LteFddStandardRawKpiMapping denominator;

    @ManyToOne
    @JoinColumn(name = "lte_fdd_basic_kpi_id")
    private LteFddBasicKpi lteFddBasicKpi;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
