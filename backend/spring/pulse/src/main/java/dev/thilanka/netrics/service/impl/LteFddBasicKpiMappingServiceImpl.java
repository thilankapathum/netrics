package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddBasicKpiMappingDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpiMapping;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiMappingRepository;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.service.LteFddBasicKpiMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddBasicKpiMappingServiceImpl implements LteFddBasicKpiMappingService {
//    private final LteFddBasicKpiMappingRepository lteFddBasicKpiMappingRepository;
//    private final LteFddBasicKpiRepository lteFddBasicKpiRepository;
//    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
//    private final Mapper mapper;
//
//    @Override
//    public List<LteFddBasicKpiMappingDto> getAll() {
//        List<LteFddBasicKpiMapping> kpiMappings = lteFddBasicKpiMappingRepository.findAll();
//
//        List<LteFddBasicKpiMappingDto> dtos = kpiMappings
//                .stream()
//                .map(mapping -> mapper.lteFddBasicKpiMappingToDto(mapping))
//                .toList();
//
//        return dtos;
//    }
//
//    @Override
//    public LteFddBasicKpiMappingDto createMapping(LteFddBasicKpiMappingDto dto) {
//
//        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository
//                .findByKpiName(dto.basicKpi())
//                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + dto.basicKpi()));
//
//        LteFddStandardKpi standardKpi = lteFddStandardKpiRepository
//                .findByKpiName(dto.standardKpi())
//                .orElseThrow(() -> new RuntimeException("Standard KPI not found by: " + dto.standardKpi()));
//
////        LteFddBasicKpiMapping mapping = LteFddBasicKpiMapping.builder()
////                .basicKpi(basicKpi)
////                .standardKpi(standardKpi)
////                .build();
//        LteFddBasicKpiMapping mapping = new LteFddBasicKpiMapping();
//
//        LteFddBasicKpiMapping savedMapping = lteFddBasicKpiMappingRepository.save(mapping);
//
//        LteFddBasicKpiMappingDto savedDto = mapper.lteFddBasicKpiMappingToDto(savedMapping);
//        return savedDto;
//    }
}
