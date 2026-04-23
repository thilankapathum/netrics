package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "map_cell_thr_sets")
public class MapCellThrSet extends AuditEntity {
    //-- Map Cell Threshold Set

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "standard_kpi_id", nullable = false)
    private StandardKpi standardKpi;

    @ManyToOne
    @JoinColumn(name = "granularity_id", nullable = false)
    private Granularity granularity;

    @ManyToOne
    @JoinColumn(name = "rat_id", nullable = false)
    private Rat rat;

    @Column(nullable = false)
    private String userId;

    private boolean isAdmin;
    private boolean isActive;
    private boolean isDeleted;

    @OneToMany(mappedBy = "mapCellThrSet", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MapCellThreshold> mapCellThresholds;
}
