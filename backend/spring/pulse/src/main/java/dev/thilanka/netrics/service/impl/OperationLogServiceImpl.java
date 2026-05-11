package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.OperationLogResponse;
import dev.thilanka.netrics.entity.common.OperationLogFilter;
import dev.thilanka.netrics.entity.common.OperationLogSpec;
import dev.thilanka.netrics.repository.OperationLogRepository;
import dev.thilanka.netrics.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Page<OperationLogResponse> query(OperationLogFilter filter, Pageable pageable) {
        return repository
                .findAll(OperationLogSpec.from(filter), pageable)
                .map(log -> new OperationLogResponse(
                        log.getId(),
                        log.getEntityName(),
                        log.getEntityId(),
                        log.getOperation(),
                        log.getPerformedAt(),
                        log.getPerformedBy(),
                        log.getChanges()
                ));
    }
}
