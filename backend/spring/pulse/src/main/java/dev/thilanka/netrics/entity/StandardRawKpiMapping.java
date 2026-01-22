package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "standard_raw_kpi_mapping")
public class StandardRawKpiMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "standard_kpi_id")
    private StandardKpi standardKpi;

    @OneToOne
    @JoinColumn(name = "numerator_id")
    private StandardKpi numerator;

    @OneToOne
    @JoinColumn(name = "denominator_id")
    private StandardKpi denominator;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
