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
@Table(name = "worst_cells")
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
