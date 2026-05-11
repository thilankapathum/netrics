package dev.thilanka.netrics.entity.district;

import dev.thilanka.netrics.entity.AreaDistrictCodeMapping;
import dev.thilanka.netrics.entity.KpiDay;
import dev.thilanka.netrics.entity.KpiHour;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "district_codes")
public class DistrictCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String code;
    private String category;

    @ManyToOne
    @JoinColumn(name = "district_id")
    private District district;

    @OneToMany(mappedBy = "districtCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiDay> kpiDays;

    @OneToMany(mappedBy = "districtCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<KpiHour> kpiHours;

    @OneToMany(mappedBy = "districtCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AreaDistrictCodeMapping> areaDistrictCodeMappings;
}
