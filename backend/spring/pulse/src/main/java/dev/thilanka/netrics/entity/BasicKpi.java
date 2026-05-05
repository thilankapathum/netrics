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
@Table(name = "basic_kpi")
@SuperBuilder
@AuditLog
public class BasicKpi extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String kpiName;
    @Column(nullable = false)
    private String label;
    private String worstOrder;
    private Double threshold;
    private String aggregation;
    private String unit;

    @OneToMany(mappedBy = "basicKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StandardKpi> standardKpis;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;
}
