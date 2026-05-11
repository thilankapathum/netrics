package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.OperationLogResponse;
import dev.thilanka.netrics.entity.common.OperationLogFilter;
import dev.thilanka.netrics.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/admin/operation-log")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PULSE_DELETE')")
public class OperationLogController {

    private final OperationLogService service;

    @GetMapping
    public ResponseEntity<Page<OperationLogResponse>> query(
            OperationLogFilter filter,
            @PageableDefault(size = 20, sort = "performedAt") Pageable pageable) {

        return ResponseEntity.ok(service.query(filter, pageable));
    }
}
