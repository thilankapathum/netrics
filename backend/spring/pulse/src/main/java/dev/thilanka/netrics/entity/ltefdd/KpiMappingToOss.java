package dev.thilanka.netrics.entity.ltefdd;

import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.Rat;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lte_fdd_kpi_mapping",
uniqueConstraints = {@UniqueConstraint(columnNames = {"lte_fdd_standard_kpi_id", "oss_id"})})
public class KpiMappingToOss {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ossKpiName;
    private Double multiplicationFactor = 1.0;  //-- Setting default multiplication factor

    @ManyToOne
    @JoinColumn(name = "oss_id")
    private Oss oss;

    @ManyToOne
    @JoinColumn(name = "lteFddStandardKpi_id")
    private StandardKpi standardKpi;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
