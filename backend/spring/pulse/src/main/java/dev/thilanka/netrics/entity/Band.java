package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "bands")
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private int number;

    @Column(nullable = false)
    private String unit;

    @OneToMany(mappedBy = "band", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Cell> cells;

    @OneToMany(mappedBy = "band", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Carrier> carriers;
}
