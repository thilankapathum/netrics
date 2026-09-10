package dev.thilanka.netrics.entity;

import dev.thilanka.netrics.entity.enums.ReportJobStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_jobs")
@Getter
@Setter
public class ReportJob {
    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ReportJobStatus status;

    private String reportType;

    @Column(columnDefinition = "TEXT")
    private String paramsJson;

    private String filePath;
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String requestedBy;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ReportJobStatus.PENDING;
    }
}
