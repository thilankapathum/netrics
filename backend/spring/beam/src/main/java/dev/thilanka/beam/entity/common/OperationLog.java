package dev.thilanka.beam.entity.common;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "operation_log")
@ExcludeDefaultListeners
public class OperationLog {

    public enum OperationType {CREATE, UPDATE, DELETE}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //-- Simple class name of the affected entity, e.g. "Cell"
    @Column(nullable = false)
    private String entityName;

    //-- String representation of the entity's PK at event time.
    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation",nullable = false, length = 10)
    private OperationType operation;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes", nullable = false, columnDefinition = "jsonb")
    private Map<String, Map<String, Object>> changes;
}
