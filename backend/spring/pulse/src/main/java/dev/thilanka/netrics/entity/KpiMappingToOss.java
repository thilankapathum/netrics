package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "kpi_mapping",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"standard_kpi_id", "oss_id", "rat_id"})})
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
