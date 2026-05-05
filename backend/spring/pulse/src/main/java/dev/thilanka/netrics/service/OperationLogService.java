package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.OperationLogResponse;
import dev.thilanka.netrics.entity.common.OperationLogFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OperationLogService {
    Page<OperationLogResponse> query(OperationLogFilter filter, Pageable pageable);
}
