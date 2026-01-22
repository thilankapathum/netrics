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
@Table(name = "user_area_mapping",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id","area_id"})})
public class UserAreaMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;
}
