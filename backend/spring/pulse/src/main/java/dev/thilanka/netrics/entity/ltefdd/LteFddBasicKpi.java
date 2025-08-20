package dev.thilanka.netrics.entity.ltefdd;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lte_fdd_basic_kpi")
public class LteFddBasicKpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String kpiName;
    @Column(unique = true,nullable = false)
    private String label;
    private String worstOrder;
    private Double threshold;
    private String aggregation;

    @OneToMany(mappedBy = "lteFddBasicKpi", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddStandardKpi> lteFddStandardKpis;
}
