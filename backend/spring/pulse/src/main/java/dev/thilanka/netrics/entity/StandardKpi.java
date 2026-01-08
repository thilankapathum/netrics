package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "standard_kpi",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"kpi_name", "label", "rat_id"})})
public class StandardKpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String kpiName;
    @Column(nullable = false)
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
    @JoinColumn(name = "basic_kpi_id")
    private BasicKpi basicKpi;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;

    @OneToMany(mappedBy = "standardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorstCell> worstCells;
}
