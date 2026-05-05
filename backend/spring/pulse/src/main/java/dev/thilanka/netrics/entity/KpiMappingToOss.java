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
@Table(name = "kpi_mapping")
@SuperBuilder
@AuditLog
public class KpiMappingToOss extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ossKpiName;
    private Double multiplicationFactor = 1.0;  //-- Setting default multiplication factor

    @ManyToOne
    @JoinColumn(name = "oss_id")
    private Oss oss;

    @ManyToOne
    @JoinColumn(name = "standard_kpi_id")
    private StandardKpi standardKpi;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
