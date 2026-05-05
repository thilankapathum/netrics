package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import dev.thilanka.netrics.entity.common.AuditLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "standard_kpi")
@SuperBuilder
@AuditLog
public class StandardKpi extends AuditEntity {
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

    @OneToMany(mappedBy = "standardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiHour> kpiHours;

    @OneToMany(mappedBy = "numeratorKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiHour> kpiHoursNumerator;

    @OneToMany(mappedBy = "denominatorKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiHour> kpiHoursDenominator;


    @OneToOne(mappedBy = "standardKpi",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private StandardRawKpiMapping standardKpi;

    @OneToOne(mappedBy = "numerator", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private StandardRawKpiMapping numerator;

    @OneToOne(mappedBy = "denominator", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private StandardRawKpiMapping denominator;

    @OneToMany(mappedBy = "standardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MapCellThrSet> mapCellThrSets;

    @ManyToOne
    @JoinColumn(name = "basic_kpi_id")
    private BasicKpi basicKpi;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;

    @OneToMany(mappedBy = "standardKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorstCell> worstCells;
}
