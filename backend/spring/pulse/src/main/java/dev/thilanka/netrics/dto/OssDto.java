package dev.thilanka.netrics.dto;

import jakarta.persistence.Column;

public record OssDto(
        String ossName,
        String identifier,
        String vendor
) {
}
