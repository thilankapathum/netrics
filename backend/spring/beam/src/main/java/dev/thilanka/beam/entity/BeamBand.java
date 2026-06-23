package dev.thilanka.beam.entity;

import dev.thilanka.beam.entity.common.AuditEntity;
import dev.thilanka.beam.entity.common.AuditLog;
import jakarta.persistence.*;
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
@Table(name = "bands")
@SuperBuilder
@AuditLog
public class BeamBand extends AuditEntity {
    //-- TODO: Implement Kafka Consumer
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private int number;

    @Column(nullable = false)
    private String unit;
}
