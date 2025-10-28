package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.district.DistrictCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lte_fdd_kpi_day",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"timestamp","cellName","lte_fdd_standard_kpi_id", "oss_id","rat_id"})})
public class KpiDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String cellName;
    private String siteName;

    private Double kpiValue;
    private Double numeratorKpiValue;
    private Double denominatorKpiValue;

    private String dataType;
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "lte_fdd_standard_kpi_id")
    private StandardKpi standardKpi;

    @ManyToOne
    @JoinColumn(name = "numerator_kpi_id")
    private StandardKpi numeratorKpi;

    @ManyToOne
    @JoinColumn(name = "denominator_kpi_id")
    private StandardKpi denominatorKpi;

    @ManyToOne
    @JoinColumn(name = "oss_id")
    private Oss oss;

    @ManyToOne
    @JoinColumn(name = "district_code_id")
    private DistrictCode districtCode;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
