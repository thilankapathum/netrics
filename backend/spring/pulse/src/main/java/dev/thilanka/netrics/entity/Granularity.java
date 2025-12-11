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
@Table(name = "granularity",
        indexes = {
                @Index(
                        name = "idx_name_label",
                        columnList = "name, label"
                )})
public class Granularity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String label;

    @OneToMany(mappedBy = "granularity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<KpiDay> kpiDays;
}
