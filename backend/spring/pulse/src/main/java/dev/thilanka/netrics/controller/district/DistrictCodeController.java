package dev.thilanka.netrics.controller.district;

import dev.thilanka.netrics.dto.DistrictCodeDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.service.DistrictCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/pulse/district-codes")
@RequiredArgsConstructor
public class DistrictCodeController {
    private final DistrictCodeService districtCodeService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping
    ResponseEntity<List<DistrictCodeDto>> getAll(){
        return new ResponseEntity<>(districtCodeService.getAll(), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    ResponseEntity<DistrictCodeDto> createDistrictCode(@RequestBody @Valid DistrictCodeDto dto){
        return new ResponseEntity<>(districtCodeService.createDistrictCode(dto),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("list")
    ResponseEntity<List<DistrictCodeDto>> createDistricCodetList(@RequestBody @Valid List<DistrictCodeDto> dtos){
        return new ResponseEntity<>(districtCodeService.createDistrictCodeList(dtos),HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    @GetMapping("update-missing")
    ResponseEntity<String> updateKpiWithoutDistrictCodes(){
        log.info("Updating KPI without district codes...");
        districtCodeService.updateKpiDayWithoutDistrict();
        log.info("Updated KPI without district codes!");
        return new ResponseEntity<>("Updated KPI without district codes", HttpStatus.OK);
    }
}
