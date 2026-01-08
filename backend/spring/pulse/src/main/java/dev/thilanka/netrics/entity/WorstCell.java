package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "worst_cells",
        indexes = {
                @Index(
                        name = "idx_worst_cell_timestamp",
                        columnList = "timestamp"
                ),
                @Index(
                        name = "idx_worst_cell_cell_name",
                        columnList = "cell_name"
                ),
                @Index(
                        name = "idx_worst_cell_area",
                        columnList = "area_id"
                ),
                @Index(
                        name = "idx_worst_cell_rat",
                        columnList = "rat_id"
                ),
                @Index(
                        name = "idx_worst_cell_kpi",
                        columnList = "standard_kpi_id"
                ),
                @Index(
                        name = "idx_exclude_zeroes",
                        columnList = "exclude_zeroes"
                ),
                @Index(
                        name = "idx_granularity",
                        columnList = "granularity_id"
                )},

        uniqueConstraints = {@UniqueConstraint(columnNames = {"timestamp", "period", "cell_name", "standard_kpi_id", "rat_id", "area_id", "exclude_zeroes","granularity_id"})})
public class WorstCell {
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
