package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cell_names")
public class CellName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cellName;
    @Column(nullable = false)
    private String ratName;
    @Column(nullable = false)
    private String ratLabel;

}
