package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiMappingToOssDto;
import dev.thilanka.netrics.entity.Oss;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.KpiMappingToOss;
import dev.thilanka.netrics.entity.ltefdd.StandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.KpiMappingRepository;
import dev.thilanka.netrics.repository.StandardKpiRepository;
import dev.thilanka.netrics.repository.OssRepository;
import dev.thilanka.netrics.service.KpiMappingToOssService;
import dev.thilanka.netrics.service.StandardKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KpiMappingToOssServiceImpl implements KpiMappingToOssService {
    private final KpiMappingRepository kpiMappingRepository;
    private final StandardKpiRepository standardKpiRepository;
    private final StandardKpiService standardKpiService;
    private final RatService ratService;
    private final OssRepository ossRepository;
    private final Mapper mapper;

    @Override
    public List<KpiMappingToOssDto> getAll(String ratName) {

        Rat rat = ratService.findRatByName(ratName);

        List<KpiMappingToOss> kpi = kpiMappingRepository.findByRatId(rat.getId());

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

        StandardKpi standardKpi = standardKpiService
                .findByKpiName(dto.standardKpi(),dto.ratName());

        KpiMappingToOss kpiMappingToOss = KpiMappingToOss.builder()
                .standardKpi(standardKpi)
                .oss(oss)
                .ossKpiName(dto.ossKpiName())
                .multiplicationFactor(dto.multiplicationFactor())
                .rat(rat)
                .build();

        KpiMappingToOss savedMapping = kpiMappingRepository.save(kpiMappingToOss);

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
