package dev.thilanka.netrics.entity.district;

import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
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
    private List<LteFddKpiDay> lteFddKpiDays;
}
