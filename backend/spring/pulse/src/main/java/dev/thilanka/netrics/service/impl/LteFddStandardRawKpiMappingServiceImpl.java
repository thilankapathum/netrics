package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.StandardKpi;
import dev.thilanka.netrics.entity.ltefdd.StandardRawKpiMapping;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddStandardRawKpiMappingRepository;
import dev.thilanka.netrics.service.StandardKpiService;
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
    private final StandardKpiService standardKpiService;

    @Override
    public List<StandardRawKpiMappingDto> getAll(String ratName) {

        Rat rat = ratService.findRatByName(ratName);
        List<StandardRawKpiMapping> mappings = lteFddStandardRawKpiMappingRepository.findByRat(rat);

        return mappings
                .stream()
                .map(mapper::lteFddStandardRawKpiMappingToDto)
                .toList();
    }

    @Override
    public StandardRawKpiMappingDto createMapping(StandardRawKpiMappingDto dto) {

        Rat rat = ratService.findRatByName(dto.ratName());

        StandardKpi standardKpi = standardKpiService.findByKpiName(dto.standardKpi(), dto.ratName());
        StandardKpi numerator = standardKpiService.findByKpiName(dto.numerator(), dto.ratName());
        StandardKpi denominator = standardKpiService.findByKpiName(dto.denominator(), dto.ratName());

        StandardRawKpiMapping mapping = StandardRawKpiMapping.builder()
                .standardKpi(standardKpi)
                .numerator(numerator)
                .denominator(denominator)
                .rat(rat)
                .build();

        StandardRawKpiMapping savedMapping = lteFddStandardRawKpiMappingRepository.save(mapping);
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
