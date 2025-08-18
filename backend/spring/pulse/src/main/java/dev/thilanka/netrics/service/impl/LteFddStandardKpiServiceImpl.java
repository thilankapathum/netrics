package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddStandardKpiDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddStandardKpiServiceImpl implements LteFddStandardKpiService {
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final Mapper mapper;

    @Override
    public List<LteFddStandardKpiDto> getAll() {
        List<LteFddStandardKpi> kpis = lteFddStandardKpiRepository.findAll();

        return kpis
                .stream()
                .map(mapper::lteFddStandardKpiToDto)
                .toList();
    }

    @Override
    public LteFddStandardKpiDto createKpi(LteFddStandardKpiDto kpiDto) {
        LteFddStandardKpi kpi = mapper.toLteFddStandardKpi(kpiDto);
        LteFddStandardKpi savedKpi = lteFddStandardKpiRepository.save(kpi);
        return mapper.lteFddStandardKpiToDto(savedKpi);
    }

    @Override
    public List<LteFddStandardKpiDto> createKpis(List<LteFddStandardKpiDto> dtos) {

        List<LteFddStandardKpiDto> dtoList = new ArrayList<>();

        for (LteFddStandardKpiDto dto : dtos){
            dtoList.add(createKpi(dto));
        }
        return dtoList;
    }

    @Override
    public LteFddStandardKpi findByKpiName(String kpiName) {
        return lteFddStandardKpiRepository
                .findByKpiName(kpiName)
                .orElseThrow(() -> new RuntimeException("Standard KPI not found by: " + kpiName));
    }
}
