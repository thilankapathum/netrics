package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardRawKpiMapping;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddStandardRawKpiMappingRepository;
import dev.thilanka.netrics.repository.RatRepository;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import dev.thilanka.netrics.service.LteFddStandardRawKpiMappingService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddStandardRawKpiMappingServiceImpl implements LteFddStandardRawKpiMappingService {
    private final LteFddStandardRawKpiMappingRepository lteFddStandardRawKpiMappingRepository;
    private final RatService ratService;
    private final Mapper mapper;
    private final LteFddStandardKpiService lteFddStandardKpiService;

    @Override
    public List<StandardRawKpiMappingDto> getAll(String ratName) {

        Rat rat = ratService.findRatByName(ratName);
        List<LteFddStandardRawKpiMapping> mappings = lteFddStandardRawKpiMappingRepository.findByRat(rat);

        return mappings
                .stream()
                .map(mapper::lteFddStandardRawKpiMappingToDto)
                .toList();
    }

    @Override
    public StandardRawKpiMappingDto createMapping(StandardRawKpiMappingDto dto) {

        Rat rat = ratService.findRatByName(dto.ratName());

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(dto.standardKpi(), dto.ratName());
        LteFddStandardKpi numerator = lteFddStandardKpiService.findByKpiName(dto.numerator(), dto.ratName());
        LteFddStandardKpi denominator = lteFddStandardKpiService.findByKpiName(dto.denominator(), dto.ratName());

        LteFddStandardRawKpiMapping mapping = LteFddStandardRawKpiMapping.builder()
                .standardKpi(standardKpi)
                .numerator(numerator)
                .denominator(denominator)
                .rat(rat)
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
