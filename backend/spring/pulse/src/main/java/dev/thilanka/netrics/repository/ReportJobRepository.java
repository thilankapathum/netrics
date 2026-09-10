package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.ReportJob;
import dev.thilanka.netrics.entity.enums.ReportJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReportJobRepository extends JpaRepository<ReportJob, UUID> {
    List<ReportJob> findByStatusAndCreatedAtBefore(ReportJobStatus status, LocalDateTime cutoff);
}
