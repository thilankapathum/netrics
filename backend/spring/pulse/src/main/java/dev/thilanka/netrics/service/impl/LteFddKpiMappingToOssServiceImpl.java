package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiMappingToOssDto;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiMappingToOss;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddKpiMappingRepository;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.repository.OssRepository;
import dev.thilanka.netrics.service.LteFddKpiMappingToOssService;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddKpiMappingToOssServiceImpl implements LteFddKpiMappingToOssService {
    private final LteFddKpiMappingRepository lteFddKpiMappingRepository;
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final LteFddStandardKpiService lteFddStandardKpiService;
    private final RatService ratService;
    private final OssRepository ossRepository;
    private final Mapper mapper;

    @Override
    public List<KpiMappingToOssDto> getAll(String ratName) {

        Rat rat = ratService.findRatByName(ratName);

        List<LteFddKpiMappingToOss> kpi = lteFddKpiMappingRepository.findByRatId(rat.getId());

        return kpi
                .stream()
                .map(mapper::LteFddKpiMappingToDto)
                .toList();
    }

    @Override
    public KpiMappingToOssDto createLteFddKpiMapping(KpiMappingToOssDto dto) {

        Rat rat = ratService.findRatByName(dto.ratName());

        Oss oss = ossRepository.findByIdentifier(dto.ossIdentifier())
                .orElseThrow(() -> new RuntimeException("OSS not found by: " + dto.ossIdentifier()));

        LteFddStandardKpi lteFddStandardKpi = lteFddStandardKpiService
                .findByKpiName(dto.standardKpi(),dto.ratName());

        LteFddKpiMappingToOss lteFddKpiMappingToOss = LteFddKpiMappingToOss.builder()
                .lteFddStandardKpi(lteFddStandardKpi)
                .oss(oss)
                .ossKpiName(dto.ossKpiName())
                .multiplicationFactor(dto.multiplicationFactor())
                .rat(rat)
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
