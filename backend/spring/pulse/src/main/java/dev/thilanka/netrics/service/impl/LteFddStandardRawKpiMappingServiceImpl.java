package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
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
    public List<StandardRawKpiMappingDto> getAll() {
        List<LteFddStandardRawKpiMapping> mappings = lteFddStandardRawKpiMappingRepository.findAll();

        return mappings
                .stream()
                .map(mapper::lteFddStandardRawKpiMappingToDto)
                .toList();
    }

    @Override
    public StandardRawKpiMappingDto createMapping(StandardRawKpiMappingDto dto) {

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
    public List<StandardRawKpiMappingDto> createMappingList(List<StandardRawKpiMappingDto> dtos) {

        List<StandardRawKpiMappingDto> savedDtos = new ArrayList<>();

        for (StandardRawKpiMappingDto dto: dtos){
            savedDtos.add(createMapping(dto));
        }

        return savedDtos;
    }
}
