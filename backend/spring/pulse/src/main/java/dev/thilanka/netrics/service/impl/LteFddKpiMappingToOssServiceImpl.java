package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddKpiMappingToOssDto;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiMappingToOss;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddKpiMappingRepository;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.repository.OssRepository;
import dev.thilanka.netrics.service.LteFddKpiMappingToOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddKpiMappingToOssServiceImpl implements LteFddKpiMappingToOssService {
    private final LteFddKpiMappingRepository lteFddKpiMappingRepository;
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final OssRepository ossRepository;
    private final Mapper mapper;

    @Override
    public List<LteFddKpiMappingToOssDto> getAll() {
        List<LteFddKpiMappingToOss> kpi = lteFddKpiMappingRepository.findAll();
        List<LteFddKpiMappingToOssDto> dtos = kpi
                .stream()
                .map(k -> mapper.LteFddKpiMappingToDto(k))
                .toList();

        return dtos;
    }

    @Override
    public LteFddKpiMappingToOssDto createLteFddKpiMapping(LteFddKpiMappingToOssDto dto) {

        Oss oss = ossRepository.findByIdentifier(dto.ossIdentifier())
                .orElseThrow(() -> new RuntimeException("OSS not found by: " + dto.ossIdentifier()));

        LteFddStandardKpi lteFddStandardKpi = lteFddStandardKpiRepository
                .findByKpiName(dto.lteFddStandardKpi())
                .orElseThrow(() -> new RuntimeException("KPI not found by: " + dto.lteFddStandardKpi()));

        LteFddKpiMappingToOss lteFddKpiMappingToOss = LteFddKpiMappingToOss.builder()
                .lteFddStandardKpi(lteFddStandardKpi)
                .oss(oss)
                .ossKpiName(dto.ossKpiName())
                .multiplicationFactor(dto.multiplicationFactor())
                .build();

        LteFddKpiMappingToOss savedMapping = lteFddKpiMappingRepository.save(lteFddKpiMappingToOss);

        LteFddKpiMappingToOssDto savedDto = mapper.LteFddKpiMappingToDto(savedMapping);

        return savedDto;
    }

    @Override
    public List<LteFddKpiMappingToOssDto> createLteFddKpiMappingList(List<LteFddKpiMappingToOssDto> dtos) {

        List<LteFddKpiMappingToOssDto> dtoList = new ArrayList<>();

        for (LteFddKpiMappingToOssDto dto : dtos){
            dtoList.add(createLteFddKpiMapping(dto));
        }
        return dtoList;
    }
}
