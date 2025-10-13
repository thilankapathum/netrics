package dev.thilanka.netrics.entity;


import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "rat")
public class Rat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String label;

    @OneToMany(mappedBy = "rat", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LteFddKpiDay> kpiDays;
}
