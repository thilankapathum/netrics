package dev.thilanka.netrics.entity.ltefdd;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lte_fdd_standard_raw_kpi_mapping")
public class LteFddStandardRawKpiMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "standard_kpi_id")
    private LteFddStandardKpi standardKpi;

    @OneToOne
    @JoinColumn(name = "numerator_id")
    private LteFddStandardKpi numerator;

    @OneToOne
    @JoinColumn(name = "denominator_id")
    private LteFddStandardKpi denominator;
}
