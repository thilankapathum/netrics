package dev.thilanka.netrics.entity.ltefdd;

import dev.thilanka.netrics.entity.Oss;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "lte_fdd_kpi_mapping")
public class LteFddKpiMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ossKpiName;
    private Double multiplicationFactor = 1.0;  //-- Setting default multiplication factor

    @ManyToOne
    @JoinColumn(name = "oss_id")
    private Oss oss;

    @ManyToOne
    @JoinColumn(name = "lteFddStandardKpi_id")
    private LteFddStandardKpi lteFddStandardKpi;
}
