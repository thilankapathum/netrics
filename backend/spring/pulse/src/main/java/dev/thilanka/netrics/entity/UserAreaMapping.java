package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.common.AuditEntity;
import dev.thilanka.netrics.entity.common.AuditLog;
import dev.thilanka.netrics.entity.district.DistrictCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_area_mapping",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id","area_id"})})
@SuperBuilder
@AuditLog
public class UserAreaMapping extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;
}
