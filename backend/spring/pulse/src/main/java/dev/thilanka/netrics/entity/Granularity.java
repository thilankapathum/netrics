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
@Table(name = "granularity")
@SuperBuilder
@AuditLog
public class Granularity extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String label;

    @Column(nullable = true)
    private int plusSeconds;

    @Column(nullable = true)
    private int windowSeconds;

    @OneToMany(mappedBy = "granularity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<KpiDay> kpiDays;

    @OneToMany(mappedBy = "granularity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<KpiHour> kpiHours;

    @OneToMany(mappedBy = "granularity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<WorstCell> worstCells;

    @OneToMany(mappedBy = "granularity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MapCellThrSet> mapCellThrSets;
}
