package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import dev.thilanka.netrics.entity.common.AuditLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "worst_cells")
@SuperBuilder
@AuditLog
public class WorstCell extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String cellName;
    private Double value;
    private Double previousValue;
    private Double difference;
    private boolean improved;

    private String period;
    private boolean excludeZeroes;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;

    @ManyToOne
    @JoinColumn(name = "standard_kpi_id")
    private StandardKpi standardKpi;

    @OneToMany(mappedBy = "worstCell", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorstCellComment> worstCellComments;

    @ManyToOne
    @JoinColumn(name = "granularity_id")
    private Granularity granularity;
}
