package dev.thilanka.beam.dto;

import dev.thilanka.beam.entity.common.AuditEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Read-only audit trail; *By fields hold the Keycloak user id (sub) or SYSTEM */
public record AuditDto(
        Instant createdAt,
        String createdBy,
        Instant lastModifiedAt,
        String lastModifiedBy
) {
    public static AuditDto of(AuditEntity entity) {
        return new AuditDto(
                toInstant(entity.getCreatedAt()),
                entity.getCreatedBy(),
                toInstant(entity.getLastModifiedAt()),
                entity.getLastModifiedBy()
        );
    }

    // audit timestamps are written in the JVM zone; send them as UTC so clients can compute relative times
    private static Instant toInstant(LocalDateTime time) {
        return time == null ? null : time.atZone(ZoneId.systemDefault()).toInstant();
    }
}
