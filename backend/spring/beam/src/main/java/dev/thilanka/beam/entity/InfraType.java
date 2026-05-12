package dev.thilanka.beam.entity;

import dev.thilanka.beam.entity.common.AuditEntity;
import dev.thilanka.beam.entity.common.AuditLog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "infra_types")
@SuperBuilder
@AuditLog
public class InfraType extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String infraType;
    private String legType;

    @OneToMany(mappedBy = "infraType", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<BeamSite> sites;
}
