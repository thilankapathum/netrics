package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import dev.thilanka.netrics.entity.common.AuditLog;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "oss")
@SuperBuilder
@AuditLog
public class Oss extends AuditEntity {
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

    @OneToMany(mappedBy = "oss", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiHour> kpiHours;
}
