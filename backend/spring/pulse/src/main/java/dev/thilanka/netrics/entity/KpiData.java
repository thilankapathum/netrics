package dev.thilanka.netrics.entity;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KpiData {
    private Timestamp timestamp;
    private String cellName;
    private String kpiLabel;
    private Double kpiValue;
}
