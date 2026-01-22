package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.district.DistrictCode;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "area_district_code_mapping")
public class AreaDistrictCodeMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne
    @JoinColumn(name = "district_code_id")
    private DistrictCode districtCode;
}
