package dev.thilanka.netrics.dto;

import dev.thilanka.netrics.entity.enums.ReportJobStatus;

import java.util.UUID;

public record ReportJobStatusDto(UUID jobId, ReportJobStatus status, String errorMessage, String fileName) {
}
