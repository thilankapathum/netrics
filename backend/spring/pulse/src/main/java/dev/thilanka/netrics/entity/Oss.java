package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "oss")
public class Oss {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String ossName;
    @Column(unique = true,nullable = false)
    private String identifier;
    @Column(nullable = true)
    private String vendor;
}
