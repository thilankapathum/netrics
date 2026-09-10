package dev.thilanka.netrics.entity.alarms;

import dev.thilanka.netrics.entity.enums.CanonicalAlarmField;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "alarm_field_mappings")
@Builder
public class AlarmFieldMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alarm_source_id", nullable = false)
    private AlarmSource alarmSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanonicalAlarmField canonicalField;

    private String sourceColumn;
    private String extractionRegex;
    private String defaultValue;

    @Column(nullable = false)
    private boolean isRequired;
}
