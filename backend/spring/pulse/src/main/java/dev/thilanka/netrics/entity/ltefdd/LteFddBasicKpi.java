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

    @OneToMany(mappedBy = "basicKpi", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<LteFddBasicKpiMapping> lteFddBasicKpiMappings;
}
