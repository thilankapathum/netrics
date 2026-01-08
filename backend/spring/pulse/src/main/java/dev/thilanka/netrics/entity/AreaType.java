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
@Table(name = "area_types")
public class AreaType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;    // District, Region, Engineer, RGO Region

    @OneToMany(mappedBy = "areaType", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Area> areas;
}
