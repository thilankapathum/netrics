package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.KpiData;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.KpiHourRepository;
import dev.thilanka.netrics.service.KpiHourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiHourServiceImpl implements KpiHourService {
    private final KpiHourRepository kpiHourRepository;
    private final Mapper mapper;

    @Override
    public List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName, String granularityName) {
        return List.of();
    }

    @Override
    public List<KpiDataDto> getDataByKpiAndCell(Long standardKpiId, String cellName, LocalDateTime timestamp, LocalDateTime startTimestamp, Long ratId, Long granularityId) {

        List<KpiData> kpiData = kpiHourRepository.findDataByKpiAndCell(standardKpiId, timestamp, startTimestamp, cellName, ratId, granularityId);
        return kpiData.stream()
                .map(mapper::kpiDataToDto)
                .collect(Collectors.toList());
    }
}
