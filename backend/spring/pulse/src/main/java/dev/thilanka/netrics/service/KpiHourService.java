package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataWithOperandsDto;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface KpiHourService {

    List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName, String granularityName);

    List<KpiDataDto> getDataByKpiAndCell(Long standardKpiId, String cellName, LocalDateTime timestamp, LocalDateTime startTimestamp, Long ratId, Long granularityId);

    List<KpiDataWithOperandsDto> getDataByKpiAndCellWithOperands(Long standardKpiId, String cellName, LocalDateTime timestamp, LocalDateTime startTimestamp, Long ratId, Long granularityId);
}
