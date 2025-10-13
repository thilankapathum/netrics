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
public class StandardKpi {
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

    @OneToMany(mappedBy = "standardKpi",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiMappingToOss> kpiMappingToOsses;

    @OneToMany(mappedBy = "standardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiDay> kpiDays;

    @OneToMany(mappedBy = "numeratorKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiDay> kpiDaysNumerator;

    @OneToMany(mappedBy = "denominatorKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiDay> kpiDaysDenominator;


    @OneToOne(mappedBy = "standardKpi",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private StandardRawKpiMapping standardKpi;

    @OneToOne(mappedBy = "numerator", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private StandardRawKpiMapping numerator;

    @OneToOne(mappedBy = "denominator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private StandardRawKpiMapping denominator;

    @ManyToOne
    @JoinColumn(name = "lte_fdd_basic_kpi_id")
    private BasicKpi basicKpi;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
