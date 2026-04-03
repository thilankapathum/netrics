package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.CarrierDto;
import dev.thilanka.netrics.service.CarrierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/carriers")
@RequiredArgsConstructor
public class CarrierController {
    private final CarrierService carrierService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PostMapping
    public ResponseEntity<CarrierDto> createCarrier(@RequestBody @Valid CarrierDto carrierDto) {
        return ResponseEntity.ok(carrierService.createCarrier(carrierDto));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PostMapping("list")
    public ResponseEntity<List<CarrierDto>> createCarriers(@RequestBody @Valid List<CarrierDto> carrierDtos) {
        return ResponseEntity.ok(carrierService.createCarriers(carrierDtos));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("{name}")
    public ResponseEntity<CarrierDto> getCarrier(@PathVariable("name") String name) {
        return ResponseEntity.ok(carrierService.getByName(name));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("all")
    public ResponseEntity<List<CarrierDto>> getAllCarriers() {
        return ResponseEntity.ok(carrierService.getAll());
    }

}
