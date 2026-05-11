package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import dev.thilanka.netrics.entity.common.AuditLog;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carriers")
@SuperBuilder
@AuditLog
public class Carrier extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;    //  Eg: L18_F1, L26_F5, N35_F1

    @Column(nullable = false)
    @Min(0)
    private Integer radius; // Cell radius for displaying in maps

    @ManyToOne
    @JoinColumn(name = "rat_id")
    private Rat rat;

    @ManyToOne
    @JoinColumn(name = "band_id")
    private Band band;

    @OneToMany(mappedBy = "carrier", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Cell> cells;
}
