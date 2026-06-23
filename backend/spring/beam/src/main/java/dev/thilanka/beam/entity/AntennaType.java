package dev.thilanka.beam.entity;

import dev.thilanka.beam.entity.common.AuditEntity;
import dev.thilanka.beam.entity.common.AuditLog;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = "antenna_types")
@SuperBuilder
@AuditLog
public class AntennaType extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Min(1)
    private int portCount;

    private int height;
    private int width;
    private boolean retAvailability;

    @OneToMany(mappedBy = "antennaType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Antenna> antennas;
}
