package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
                )},

        uniqueConstraints = {@UniqueConstraint(columnNames = {"timestamp", "period", "cell_name", "standard_kpi_id", "rat_id", "area_id"})})
public class WorstCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String cellName;
    //        private String kpiLabel;
    private String unit;
    private Double value;
    private Double previousValue;
    private Double difference;
    private boolean improved;

    private String period;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;
//    private String areaAggregation;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;

    @ManyToOne
    @JoinColumn(name = "standard_kpi_id")
    private StandardKpi standardKpi;
}
