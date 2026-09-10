package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.dto.UserAreaMappingUpdateDto;
import dev.thilanka.netrics.service.UserAreaMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("/all")
    ResponseEntity<List<UserAreaMappingDto>> getAllUserAreaMappings(){
        return ResponseEntity.ok(userAreaMappingService.getAllUserAreaMappings());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PutMapping("/{id}")
    ResponseEntity<UserAreaMappingDto> updateUserAreaMapping(@PathVariable("id") Long id,
                                                              @RequestBody @Valid UserAreaMappingUpdateDto dto){
        return ResponseEntity.ok(userAreaMappingService.updateUserAreaMapping(id, dto));
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUserAreaMapping(@PathVariable("id") Long id){
        userAreaMappingService.deleteUserAreaMapping(id);
        return ResponseEntity.noContent().build();
    }
}
