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
@Table(name = "rat")
@SuperBuilder
@AuditLog
public class Rat extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String label;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiDay> kpiDays;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiHour> kpiHours;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BasicKpi> basicKpis;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StandardKpi> standardKpis;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<StandardRawKpiMapping> kpiMappings;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiMappingToOss> kpiMappingToOsses;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorstCell> worstCells;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Cell> cells;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Carrier> carriers;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MapCellThrSet> mapCellThrSets;
}
