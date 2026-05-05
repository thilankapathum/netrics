package dev.thilanka.netrics.repository;

import dev.thilanka.netrics.entity.common.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long>,
        JpaSpecificationExecutor<OperationLog> {
    Page<OperationLog> findByEntityName(String entityName, Pageable pageable);

    Page<OperationLog> findByEntityNameAndEntityId(String entityName,
                                                   String entityId,
                                                   Pageable pageable);

    Page<OperationLog> findByPerformedBy(String performedBy, Pageable pageable);

    Page<OperationLog> findByPerformedAtBetween(LocalDateTime from,
                                                LocalDateTime to,
                                                Pageable pageable);

    Page<OperationLog> findByOperation(OperationLog.OperationType operation,
                                       Pageable pageable);
}
