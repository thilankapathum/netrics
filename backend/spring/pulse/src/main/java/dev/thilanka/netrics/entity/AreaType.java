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
@Table(name = "area_types")
@SuperBuilder
@AuditLog
public class AreaType extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;    // District, Region, Engineer, RGO Region

    @OneToMany(mappedBy = "areaType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Area> areas;
}
