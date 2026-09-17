package dev.thilanka.netrics.assistant;

import dev.thilanka.netrics.dto.AssistantRequestDto;
import dev.thilanka.netrics.dto.AssistantResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pulse/assistant")
@RequiredArgsConstructor
public class AssistantController {
    private final AssistantService assistantService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_ASSISTANT')")
    @PostMapping("ask")
    public ResponseEntity<AssistantResponseDto> ask(@RequestBody @Valid AssistantRequestDto dto) {
        return ResponseEntity.ok(assistantService.ask(dto.question()));
    }
}
