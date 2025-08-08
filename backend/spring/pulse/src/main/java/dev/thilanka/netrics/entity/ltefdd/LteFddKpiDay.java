package dev.thilanka.netrics.entity.ltefdd;

import dev.thilanka.netrics.entity.Oss;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lte_fdd_kpi_day",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"timestamp","cellName","lte_fdd_standard_kpi_id", "oss_id"})})
public class LteFddKpiDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private String cellName;
    private String siteName;
    private String kpiValue;
    private String dataType;
    private String fileName;

    @ManyToOne
    @JoinColumn(name = "lte_fdd_standard_kpi_id")
    private LteFddStandardKpi lteFddStandardKpi;

    @ManyToOne
    @JoinColumn(name = "oss_id")
    private Oss oss;
}
