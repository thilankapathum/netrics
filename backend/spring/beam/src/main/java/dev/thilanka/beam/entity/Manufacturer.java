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
@Table(name = "manufacturers")
@SuperBuilder
@AuditLog
public class Manufacturer extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @OneToMany(mappedBy = "manufacturer", fetch = FetchType.LAZY,cascade = CascadeType.ALL) //--mapppedBy is essential for the 'One' side of the Join table. Should map the exact property name from 'Many' side.
    private List<Antenna> antennas;
}
