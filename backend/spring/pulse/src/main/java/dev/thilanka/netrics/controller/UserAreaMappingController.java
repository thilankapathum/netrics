package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.service.UserAreaMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pulse/user-area")
@RequiredArgsConstructor
public class UserAreaMappingController {
    private final UserAreaMappingService userAreaMappingService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping
    ResponseEntity<UserAreaMappingDto> createUserAreaMapping(@RequestBody @Valid UserAreaMappingDto dto){
        UserAreaMappingDto savedUserAreaMapping = userAreaMappingService.createUserAreaMapping(dto);
        return new ResponseEntity<>(savedUserAreaMapping, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    ResponseEntity<AreaDto> getAreaByUserId(@RequestParam("userId") String userId){
        return ResponseEntity.ok(userAreaMappingService.getAreaByUserId(userId));
    }
}
