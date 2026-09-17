package dev.thilanka.netrics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantRequestDto(
        @NotBlank @Size(max = 500) String question
) {
}
