package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AlarmFieldMappingDto;
import dev.thilanka.netrics.entity.alarms.AlarmFieldMapping;
import dev.thilanka.netrics.service.AlarmFieldMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/alarms/field-mappings")
@RequiredArgsConstructor
public class AlarmFieldMappingController {

    private final AlarmFieldMappingService alarmFieldMappingService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<AlarmFieldMappingDto>> getMappingsBySource(@RequestParam String sourceName){
        List<AlarmFieldMappingDto> dtos = alarmFieldMappingService.findByAlarmSource(sourceName);
        return ResponseEntity.ok(dtos);
    }
}
