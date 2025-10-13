package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.ltefdd.KpiDay;
import dev.thilanka.netrics.entity.ltefdd.KpiMappingToOss;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "oss")
public class Oss {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ossName;
    @Column(unique = true,nullable = false)
    private String identifier;
    @Column(nullable = true)
    private String vendor;

    @OneToMany(mappedBy = "oss", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiMappingToOss> kpiMappingToOsses;

    @OneToMany(mappedBy = "oss", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiDay> kpiDays;
}
