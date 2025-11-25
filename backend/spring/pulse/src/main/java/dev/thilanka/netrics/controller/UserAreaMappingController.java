package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.service.UserAreaMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/user-area")
@RequiredArgsConstructor
public class UserAreaMappingController {
    private final UserAreaMappingService userAreaMappingService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping
    ResponseEntity<UserAreaMappingDto> createUserAreaMapping(@RequestBody @Valid UserAreaMappingDto dto){
        UserAreaMappingDto savedUserAreaMapping = userAreaMappingService.createUserAreaMapping(dto);
        return new ResponseEntity<>(savedUserAreaMapping, HttpStatus.CREATED);
    }
}
