package dev.thilanka.netrics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "assistant_query_log")
@ExcludeDefaultListeners
public class AssistantQueryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 20)
    private String status;

    private String tool;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> params;

    private Integer rowCount;

    @Column(nullable = false)
    private Integer latencyMs;

    private String error;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
