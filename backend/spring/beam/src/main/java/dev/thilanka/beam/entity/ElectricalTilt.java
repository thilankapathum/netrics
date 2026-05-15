package dev.thilanka.beam.entity;

import dev.thilanka.beam.entity.common.AuditEntity;
import dev.thilanka.beam.entity.common.AuditLog;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "electrical_tilts")
@SuperBuilder
@AuditLog
public class ElectricalTilt extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(-90)
    @Max(90)
    private byte electricalTilt;

    @ManyToOne
    @JoinColumn(name = "antenna_id")
    private Antenna antenna;

    @ManyToOne
    @JoinColumn(name = "band_id")
    private BeamBand band;
}