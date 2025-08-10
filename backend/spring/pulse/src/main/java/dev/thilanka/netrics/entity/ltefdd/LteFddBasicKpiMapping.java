package dev.thilanka.netrics.entity.ltefdd;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lte_fdd_basic_kpi_mapping",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"lte_fdd_basic_kpi_id","lte_fdd_standard_kpi_id"})})
public class LteFddBasicKpiMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lte_fdd_basic_kpi_id")
    private LteFddBasicKpi basicKpi;

    @ManyToOne
    @JoinColumn(name = "lte_fdd_standard_kpi_id")
    private LteFddStandardKpi standardKpi;
}
