package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import dev.thilanka.netrics.entity.common.AuditLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "standard_raw_kpi_mapping")
@SuperBuilder
@AuditLog
public class StandardRawKpiMapping extends AuditEntity {
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
