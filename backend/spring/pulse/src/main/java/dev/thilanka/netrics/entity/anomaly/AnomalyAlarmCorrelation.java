package dev.thilanka.netrics.entity.anomaly;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "anomaly_alarm_correlations")
public class AnomalyAlarmCorrelation {
    public enum MatchLevel { NODE, CELL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anomaly_id", nullable = false)
    private Long anomalyId;

    @Column(name = "anomaly_timestamp", nullable = false)
    private LocalDateTime anomalyTimestamp;

    @Column(name = "alarm_id", nullable = false)
    private Long alarmId;

    @Column(name = "alarm_definition_id", nullable = false)
    private Long alarmDefinitionId;

    @Column(name = "alarm_source_id", nullable = false)
    private Long alarmSourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_level", nullable = false, length = 10)
    private MatchLevel matchLevel;

    @Column(name = "overlap_seconds", nullable = false)
    private int overlapSeconds;

    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
