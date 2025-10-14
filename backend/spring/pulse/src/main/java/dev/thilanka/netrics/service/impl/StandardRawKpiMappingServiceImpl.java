package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.StandardRawKpiMappingDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.StandardKpi;
import dev.thilanka.netrics.entity.StandardRawKpiMapping;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.StandardRawKpiMappingRepository;
import dev.thilanka.netrics.service.StandardKpiService;
import dev.thilanka.netrics.service.StandardRawKpiMappingService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StandardRawKpiMappingServiceImpl implements StandardRawKpiMappingService {
    private final StandardRawKpiMappingRepository standardRawKpiMappingRepository;
    private final RatService ratService;
    private final Mapper mapper;
    private final StandardKpiService standardKpiService;

    @Override
    public List<StandardRawKpiMappingDto> getAll(String ratName) {

        Rat rat = ratService.findRatByName(ratName);
        List<StandardRawKpiMapping> mappings = standardRawKpiMappingRepository.findByRat(rat);

        return mappings
                .stream()
                .map(mapper::standardRawKpiMappingToDto)
                .toList();
    }

    @Override
    public StandardRawKpiMappingDto createMapping(StandardRawKpiMappingDto dto) {

        Rat rat = ratService.findRatByName(dto.ratName());

        StandardKpi standardKpi = standardKpiService.findByKpiName(dto.standardKpi(), rat);
        StandardKpi numerator = standardKpiService.findByKpiName(dto.numerator(), rat);
        StandardKpi denominator = standardKpiService.findByKpiName(dto.denominator(), rat);

        StandardRawKpiMapping mapping = StandardRawKpiMapping.builder()
                .standardKpi(standardKpi)
                .numerator(numerator)
                .denominator(denominator)
                .rat(rat)
                .build();

        StandardRawKpiMapping savedMapping = standardRawKpiMappingRepository.save(mapping);
        return mapper.standardRawKpiMappingToDto(savedMapping);
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
