package dev.thilanka.netrics.entity.district;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "districts")
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String code;

    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<DistrictCode> districtCodes;
}
