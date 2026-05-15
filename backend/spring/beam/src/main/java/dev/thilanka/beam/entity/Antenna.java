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

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "antennas")
@SuperBuilder
@AuditLog
public class Antenna extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Min(1)
    private byte antennaIndex;  // Unique per sector

    @Column(nullable = false)
    @Min(0)
    @Max(360)
    private short azimuth;

    @Column(nullable = false)
    @Min(-90)
    @Max(90)
    private short mechanicalTilt;

    @Max(1000)
    @Min(0)
    private short antennaHeight;

    @ManyToOne
    @JoinColumn(name = "antenna_type_id")
    private AntennaType antennaType;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @ManyToOne
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @OneToMany(mappedBy = "antenna", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ElectricalTilt> electricalTilts;
}
