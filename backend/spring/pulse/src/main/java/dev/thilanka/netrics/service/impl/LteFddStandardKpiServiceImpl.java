package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddStandardKpiDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddStandardKpiServiceImpl implements LteFddStandardKpiService {
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final Mapper mapper;

    @Override
    public List<LteFddStandardKpiDto> getAll() {
        List<LteFddStandardKpi> lteFddStandardKpis = lteFddStandardKpiRepository.findAll();
        List<LteFddStandardKpiDto> lteFddStandardKpiDtos = lteFddStandardKpis
                .stream()
                .map(lfsk -> mapper.lteFddStandardKpiToDto(lfsk))
                .toList();

        return lteFddStandardKpiDtos;
    }

    @Override
    public LteFddStandardKpiDto createLteFddStandardKpi(LteFddStandardKpiDto lteFddStandardKpiDto) {
        LteFddStandardKpi lteFddStandardKpi = mapper.toLteFddStandardKpi(lteFddStandardKpiDto);
        LteFddStandardKpi savedLteFddStandardKpi = lteFddStandardKpiRepository.save(lteFddStandardKpi);
        return mapper.lteFddStandardKpiToDto(savedLteFddStandardKpi);
    }
}
