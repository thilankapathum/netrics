package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AlarmTypeDto;
import dev.thilanka.netrics.service.AlarmTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/alarms/types")
@RequiredArgsConstructor
public class AlarmTypeController {
    private final AlarmTypeService alarmTypeService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<AlarmTypeDto>> getAll() {
        return new ResponseEntity<>(alarmTypeService.getAll(), HttpStatus.OK);
    }
}
