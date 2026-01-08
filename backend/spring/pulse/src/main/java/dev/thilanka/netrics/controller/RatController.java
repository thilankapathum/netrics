package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.RatDto;
import dev.thilanka.netrics.service.RatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pulse/rat")
public class RatController {
    private final RatService ratService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    public ResponseEntity<List<RatDto>> getAll(){
        List<RatDto> ratDtos = ratService.getAll();
        return ResponseEntity.ok(ratDtos);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    public ResponseEntity<RatDto> createRat(@RequestBody @Valid RatDto ratDto){
        RatDto savedRat = ratService.createRat(ratDto);
        return new ResponseEntity<>(savedRat, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("list")
    public ResponseEntity<List<RatDto>> createRatList(@RequestBody @Valid List<RatDto> dtos){
        List<RatDto> ratDtos = ratService.createRatList(dtos);
        return new ResponseEntity<>(ratDtos, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("name/{name}")
    public ResponseEntity<RatDto> findByName(@PathVariable("name") String name){
        RatDto ratDto = ratService.getRatDtoByName(name);
        return ResponseEntity.ok(ratDto);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("label/{label}")
    public ResponseEntity<RatDto> findByLabel(@PathVariable("label") String label){
        RatDto ratDto = ratService.getRatDtoByLabel(label);
        return ResponseEntity.ok(ratDto);
    }
}
