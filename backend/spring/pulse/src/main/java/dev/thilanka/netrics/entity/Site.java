package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String siteCode;

    @Column(nullable = false)
    String siteName;

    @Min(-90)
    @Max(90)
    Double latitude;

    @Min(-180)
    @Max(180)
    Double longitude;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Cell> cells;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Sector> sectors;
}
