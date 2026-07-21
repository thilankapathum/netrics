package dev.thilanka.netrics.dto;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int pageSize
) {
    public static <T> PagedResponse<T> of(List<T> content, long totalElements, int page, int pageSize) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
        return new PagedResponse<>(content, totalElements, totalPages, page, pageSize);
    }
}
