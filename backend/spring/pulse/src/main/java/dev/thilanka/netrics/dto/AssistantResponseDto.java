package dev.thilanka.netrics.dto;

import java.util.List;
import java.util.Map;

/**
 * status: ANSWER (rows are authoritative data, summary is AI-generated and may be null),
 * CLARIFY (message + options to pick from), REFUSED (question outside the assistant's scope).
 */
public record AssistantResponseDto(
        String status,
        String message,
        String summary,
        String tool,
        Map<String, String> params,
        List<?> rows,
        List<String> options
) {
    public static AssistantResponseDto answer(String tool, Map<String, String> params, List<?> rows, String summary) {
        return new AssistantResponseDto("ANSWER", null, summary, tool, params, rows, List.of());
    }

    public static AssistantResponseDto clarify(String message, List<String> options) {
        return new AssistantResponseDto("CLARIFY", message, null, null, Map.of(), List.of(), options);
    }

    public static AssistantResponseDto refused(String message) {
        return new AssistantResponseDto("REFUSED", message, null, null, Map.of(), List.of(), List.of());
    }
}
