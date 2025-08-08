package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddKpiMappingDto;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiMapping;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddKpiMappingRepository;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.repository.OssRepository;
import dev.thilanka.netrics.service.LteFddKpiMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LteFddKpiMappingServiceImpl implements LteFddKpiMappingService {
    private final LteFddKpiMappingRepository lteFddKpiMappingRepository;
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final OssRepository ossRepository;
    private final Mapper mapper;

    @Override
    public List<LteFddKpiMappingDto> getAll() {
        List<LteFddKpiMapping> kpi = lteFddKpiMappingRepository.findAll();
        List<LteFddKpiMappingDto> dtos = kpi
                .stream()
                .map(k -> mapper.LteFddKpiMappingToDto(k))
                .toList();

        return dtos;
    }

    @Override
    public LteFddKpiMappingDto createLteFddKpiMapping(LteFddKpiMappingDto dto) {

        Oss oss = ossRepository.findByIdentifier(dto.ossIdentifier())
                .orElseThrow(() -> new RuntimeException("OSS not found by: " + dto.ossIdentifier()));

        LteFddStandardKpi lteFddStandardKpi = lteFddStandardKpiRepository
                .findByKpiName(dto.lteFddStandardKpi())
                .orElseThrow(() -> new RuntimeException("KPI not found by: " + dto.lteFddStandardKpi()));

        LteFddKpiMapping lteFddKpiMapping = LteFddKpiMapping.builder()
                .lteFddStandardKpi(lteFddStandardKpi)
                .oss(oss)
                .ossKpiName(dto.ossKpiName())
                .multiplicationFactor(dto.multiplicationFactor())
                .build();

        LteFddKpiMapping savedMapping = lteFddKpiMappingRepository.save(lteFddKpiMapping);

        LteFddKpiMappingDto savedDto = mapper.LteFddKpiMappingToDto(savedMapping);

        return savedDto;
    }

    @Override
    public List<LteFddKpiMappingDto> createLteFddKpiMappingList(List<LteFddKpiMappingDto> dtos) {

        List<LteFddKpiMappingDto> dtoList = new ArrayList<>();

        for (LteFddKpiMappingDto dto : dtos){
            dtoList.add(createLteFddKpiMapping(dto));
        }
        return dtoList;
    }
}
