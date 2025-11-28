package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.dto.WorstCellCommentDto;
import dev.thilanka.netrics.service.WorstCellCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pulse/worst-cell-comments")
@RequiredArgsConstructor
public class WorstCellCommentController {
    private final WorstCellCommentService worstCellCommentService;

    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping
    ResponseEntity<WorstCellCommentDto> createWorstCellComment(@RequestParam("comment") String comment, @RequestParam("worstCellId") Long worstCellId){
        WorstCellCommentDto savedComment = worstCellCommentService.createWorstCellComment(comment,worstCellId);
        return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @GetMapping("cell/{id}")
    ResponseEntity<List<WorstCellCommentDto>> getWorstCellCommentByWorstCell(@PathVariable("id") Long worstCellId){
        List<WorstCellCommentDto> comments = worstCellCommentService.getCommentsByWorstCell(worstCellId);
        return ResponseEntity.ok(comments);
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_READ')")
    @PutMapping("cell/{id}")
    ResponseEntity<WorstCellCommentDto> updateWorstCellComment(@RequestBody @Valid WorstCellCommentDto dto){
        return null;
    }
}
