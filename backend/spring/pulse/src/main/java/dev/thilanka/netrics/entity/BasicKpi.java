package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "basic_kpi",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"kpi_name", "label", "rat_id"})})
public class BasicKpi {

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
