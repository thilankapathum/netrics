package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.UserDto;
import dev.thilanka.netrics.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping
    ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllRealmUsers());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @GetMapping("{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id){
        UserDto userDto = userService.getUserById(id);
        return ResponseEntity.ok(userDto);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_DELETE')")
    @PostMapping
    ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto){
        UserDto savedUser = userService.createUser(userDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}
