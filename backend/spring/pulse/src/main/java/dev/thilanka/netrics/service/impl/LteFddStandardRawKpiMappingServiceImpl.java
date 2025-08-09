package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddStandardRawKpiMappingDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardRawKpiMapping;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddStandardRawKpiMappingRepository;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import dev.thilanka.netrics.service.LteFddStandardRawKpiMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddStandardRawKpiMappingServiceImpl implements LteFddStandardRawKpiMappingService {
    private final LteFddStandardRawKpiMappingRepository lteFddStandardRawKpiMappingRepository;
    private final Mapper mapper;
    private final LteFddStandardKpiService lteFddStandardKpiService;

    @Override
    public List<LteFddStandardRawKpiMappingDto> getAll() {
        List<LteFddStandardRawKpiMapping> mappings = lteFddStandardRawKpiMappingRepository.findAll();
        List<LteFddStandardRawKpiMappingDto> dtos = mappings
                .stream()
                .map(m -> mapper.lteFddStandardRawKpiMappingToDto(m))
                .toList();

        return dtos;
    }

    @Override
    public LteFddStandardRawKpiMappingDto createMapping(LteFddStandardRawKpiMappingDto dto) {

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(dto.standardKpi());
        LteFddStandardKpi numerator = lteFddStandardKpiService.findByKpiName(dto.numerator());
        LteFddStandardKpi denominator = lteFddStandardKpiService.findByKpiName(dto.denominator());

        LteFddStandardRawKpiMapping mapping = LteFddStandardRawKpiMapping.builder()
                .standardKpi(standardKpi)
                .numerator(numerator)
                .denominator(denominator)
                .build();

        LteFddStandardRawKpiMapping savedMapping = lteFddStandardRawKpiMappingRepository.save(mapping);
        LteFddStandardRawKpiMappingDto savedDto = mapper.lteFddStandardRawKpiMappingToDto(savedMapping);
        return savedDto;
    }
}
