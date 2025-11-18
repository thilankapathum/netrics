package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.DateRangeDto;
import dev.thilanka.netrics.service.DateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/dates")
@RequiredArgsConstructor
public class DateController {
    private final DateService dateService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    ResponseEntity<DateRangeDto> getLatestDateRange(@RequestParam String period, @RequestParam String ratName){
        DateRangeDto dateRange = dateService.getLatestDateRange(period, ratName);
        return ResponseEntity.ok(dateRange);
    }
}
