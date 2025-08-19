package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiMappingToOssDto;
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
    public List<KpiMappingToOssDto> getAll() {
        List<LteFddKpiMappingToOss> kpi = lteFddKpiMappingRepository.findAll();

        return kpi
                .stream()
                .map(mapper::LteFddKpiMappingToDto)
                .toList();
    }

    @Override
    public KpiMappingToOssDto createLteFddKpiMapping(KpiMappingToOssDto dto) {

        Oss oss = ossRepository.findByIdentifier(dto.ossIdentifier())
                .orElseThrow(() -> new RuntimeException("OSS not found by: " + dto.ossIdentifier()));

        LteFddStandardKpi lteFddStandardKpi = lteFddStandardKpiRepository
                .findByKpiName(dto.standardKpi())
                .orElseThrow(() -> new RuntimeException("KPI not found by: " + dto.standardKpi()));

        LteFddKpiMappingToOss lteFddKpiMappingToOss = LteFddKpiMappingToOss.builder()
                .lteFddStandardKpi(lteFddStandardKpi)
                .oss(oss)
                .ossKpiName(dto.ossKpiName())
                .multiplicationFactor(dto.multiplicationFactor())
                .build();

        LteFddKpiMappingToOss savedMapping = lteFddKpiMappingRepository.save(lteFddKpiMappingToOss);

        return mapper.LteFddKpiMappingToDto(savedMapping);
    }

    @Override
    public List<KpiMappingToOssDto> createLteFddKpiMappingList(List<KpiMappingToOssDto> dtos) {

        List<KpiMappingToOssDto> dtoList = new ArrayList<>();

        for (KpiMappingToOssDto dto : dtos){
            dtoList.add(createLteFddKpiMapping(dto));
        }
        return dtoList;
    }
}
