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
        uniqueConstraints = {@UniqueConstraint(columnNames = {"timestamp", "period", "cell_name", "standard_kpi_id", "rat_id","area_aggregation"})})
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
    private String areaAggregation;

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;

    @ManyToOne
    @JoinColumn(name = "standard_kpi_id")
    private StandardKpi standardKpi;
}
