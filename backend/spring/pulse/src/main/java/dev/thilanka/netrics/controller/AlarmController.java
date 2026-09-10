package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AlarmFilter;
import dev.thilanka.netrics.dto.AlarmsDto;
import dev.thilanka.netrics.dto.CellAlarmDto;
import dev.thilanka.netrics.dto.PagedResponse;
import dev.thilanka.netrics.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/alarms/details")
@RequiredArgsConstructor
public class AlarmController {
    private final AlarmService alarmService;

    @GetMapping("cell")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<CellAlarmDto>> getAlarmsByCell(@RequestParam String cellName, @RequestParam String period) {
        return new ResponseEntity<>(alarmService.getAlarmsByCell(cellName, period), HttpStatus.OK);
    }

    @GetMapping("cell-granularity")
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<List<CellAlarmDto>> getAlarmsByCell(@RequestParam String cellName, @RequestParam LocalDateTime startDate, @RequestParam String granularityName) {
        return new ResponseEntity<>(alarmService.getAlarmsByCell(cellName, startDate, granularityName), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    public ResponseEntity<PagedResponse<AlarmsDto>> getAlarms(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) String nodeName,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String alarmType,
            @RequestParam(required = false) String alarmName,
            @RequestParam(required = false) String ackState,
            @RequestParam(required = false) String clearState,
            @RequestParam(required = false) String alarmSource,
            @RequestParam(required = false) String areaName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        AlarmFilter filter = new AlarmFilter(period, blankToNull(nodeName), blankToNull(severity),
                blankToNull(alarmType), blankToNull(alarmName), blankToNull(ackState),
                blankToNull(clearState), blankToNull(alarmSource), areaName);
        return ResponseEntity.ok(alarmService.getAlarms(filter, page, size));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

}
