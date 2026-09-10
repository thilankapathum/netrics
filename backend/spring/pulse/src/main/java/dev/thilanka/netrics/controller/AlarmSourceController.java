package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AlarmSourceDto;
import dev.thilanka.netrics.service.AlarmSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pulse/alarms/sources")
public class AlarmSourceController {
    private final AlarmSourceService alarmSourceService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity <List<AlarmSourceDto>> getAlarmSources() {
        return new ResponseEntity<>(alarmSourceService.getAll(), HttpStatus.OK);
    }

    @GetMapping("name")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<AlarmSourceDto> getAlarmSourceByName(@RequestParam String name) {
        return ResponseEntity.ok(alarmSourceService.getAlarmSource(name));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    public ResponseEntity<AlarmSourceDto> createAlarmSource(@RequestBody @Valid AlarmSourceDto dto) {
        return new ResponseEntity<>(alarmSourceService.createAlarmSource(dto), HttpStatus.CREATED);
    }

    @PostMapping("list")
    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    public ResponseEntity<List<AlarmSourceDto>> createAlarmSources(@RequestBody @Valid List<AlarmSourceDto> dtos) {
        return new ResponseEntity<>(alarmSourceService.createAlarmSourceDtos(dtos), HttpStatus.CREATED);
    }

}
