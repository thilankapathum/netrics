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
@Table(name = "sectors")
@SuperBuilder
@AuditLog
public class Sector extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Column(nullable = false)
    private Integer sectorIndex;  // Unique per site

    // Example: SITE01__1
    @Column(nullable = false, unique = true)
    private String name;

    @Max(360)
    @Min(0)
    private Integer azimuth;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @OneToMany(mappedBy = "sector",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<Antenna> antenna;
}
