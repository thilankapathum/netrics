package dev.thilanka.netrics.dto;

public record UserDto(
        String userId,
        String firstName,
        String lastName,
        String username,
        String email
) {
}
