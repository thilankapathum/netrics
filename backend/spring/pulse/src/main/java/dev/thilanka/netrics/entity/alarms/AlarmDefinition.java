package dev.thilanka.netrics.entity.alarms;

import dev.thilanka.netrics.entity.enums.AlarmSeverity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "alarm_definitions")
@Entity
@Builder
public class AlarmDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alarm_source_id", nullable = false)
    private AlarmSource alarmSource;

    @Column(nullable = false)
    private Long alarmCode;

    @Column(nullable = false)
    private String alarmName;

    private String description;
}
