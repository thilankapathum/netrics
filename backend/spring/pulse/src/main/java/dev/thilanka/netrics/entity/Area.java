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
@Table(name = "areas")
@SuperBuilder
@AuditLog
public class Area extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private boolean enabled;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserAreaMapping> userAreaMappings;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AreaDistrictCodeMapping> areaDistrictCodeMappings;

    @ManyToOne
    @JoinColumn(name = "area_type_id")
    private AreaType areaType;

    @OneToMany(mappedBy = "area", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorstCell> worstCells;
}
