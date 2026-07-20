package dev.thilanka.netrics.entity.alarms;

import dev.thilanka.netrics.entity.enums.AlarmAckState;
import dev.thilanka.netrics.entity.enums.AlarmClearState;
import dev.thilanka.netrics.entity.enums.AlarmSeverity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "alarms")
public class Alarms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nodeName;    // Need to filter by nodeName

    @ManyToOne
    @JoinColumn(name = "alarm_definition_id")
    private AlarmDefinition alarmDefinition;    // Alarm Code, Alarm Name

    private LocalDateTime occurrenceTime;
    private String specificProblem;

    @Enumerated(EnumType.STRING)
    private AlarmSeverity severity;     // CRITICAL, MAJOR, MINOR,...
    private String rawSeverity;

    @Enumerated(EnumType.STRING)
    private AlarmAckState ackState;     // ACKNOWLEDGED, UNACKNOWLEDGED

    private Long alarmId;

    @ManyToOne
    @JoinColumn(name = "alarm_type_id", nullable = false)
    private AlarmType alarmType;    // Performance threshold exceeded, Equipment Alarm, Communication system

    private String location;
    private String additionalInfo;
    private String description;

    @Enumerated(EnumType.STRING)
    private AlarmClearState clearState;

    private LocalDateTime clearTime;

    @ManyToOne
    @JoinColumn(name = "alarm_source_id", nullable = false)
    private AlarmSource alarmSource;
}
