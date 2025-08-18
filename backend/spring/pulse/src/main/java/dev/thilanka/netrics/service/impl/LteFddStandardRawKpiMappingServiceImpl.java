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

import java.util.ArrayList;
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

        return mappings
                .stream()
                .map(mapper::lteFddStandardRawKpiMappingToDto)
                .toList();
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
        return mapper.lteFddStandardRawKpiMappingToDto(savedMapping);
    }

    @Override
    public List<LteFddStandardRawKpiMappingDto> createMappingList(List<LteFddStandardRawKpiMappingDto> dtos) {

        List<LteFddStandardRawKpiMappingDto> savedDtos = new ArrayList<>();

        for (LteFddStandardRawKpiMappingDto dto: dtos){
            savedDtos.add(createMapping(dto));
        }

        return savedDtos;
    }
}
