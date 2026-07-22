package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_anomalies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiAnomaly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cell_name", nullable = false)
    private String cellName;

    @Column(name = "standard_kpi_id", nullable = false)
    private Long standardKpiId;

    @Column(name = "rat_id", nullable = false)
    private Long ratId;

    @Column(name = "granularity_id", nullable = false)
    private Long granularityId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "observed_value", nullable = false)
    private Double observedValue;

    @Column(name = "baseline_median", nullable = false)
    private Double baselineMedian;

    @Column(name = "mad")
    private Double mad;

    @Column(name = "robust_z_score")
    private Double robustZScore;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @Column(name = "has_alarm_correlation", nullable = false)
    private boolean hasAlarmCorrelation;

    @Column(name = "distinct_alarm_def_count", nullable = false)
    private int distinctAlarmDefCount;

    @Column(name = "total_alarm_occurrences", nullable = false)
    private int totalAlarmOccurrences;

    @Column(name = "best_match_level")
    private String bestMatchLevel;

}
