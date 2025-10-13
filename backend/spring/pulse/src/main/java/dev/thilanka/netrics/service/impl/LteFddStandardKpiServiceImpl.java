package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.service.LteFddBasicKpiService;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LteFddStandardKpiServiceImpl implements LteFddStandardKpiService {
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final LteFddBasicKpiService lteFddBasicKpiService;
    private final RatService ratService;
    private final Mapper mapper;

    @Override
    public List<StandardKpiDto> getAll() {
        List<LteFddStandardKpi> kpis = lteFddStandardKpiRepository.findAll();

        return kpis
                .stream()
                .map(mapper::lteFddStandardKpiToDto)
                .toList();
    }

    @Override
    public StandardKpiDto createKpi(StandardKpiDto kpiDto) {

        LteFddStandardKpi kpi = mapper.toLteFddStandardKpi(kpiDto);
//        Rat rat = ratService.findRatByName(kpiDto.ratName());

        if (!Objects.equals(kpiDto.basicKpi(), "")) {
            System.out.println("Basic KPI: " + kpiDto.basicKpi());
            LteFddBasicKpi basicKpi = lteFddBasicKpiService.findByKpiName(kpiDto.basicKpi(), kpiDto.ratName());
            kpi.setLteFddBasicKpi(basicKpi);
        }

        LteFddStandardKpi savedKpi = lteFddStandardKpiRepository.save(kpi);
        return mapper.lteFddStandardKpiToDto(savedKpi);
    }

    @Override
    public List<StandardKpiDto> createKpis(List<StandardKpiDto> dtos) {

        List<StandardKpiDto> dtoList = new ArrayList<>();

        for (StandardKpiDto dto : dtos) {
            dtoList.add(createKpi(dto));
        }
        return dtoList;
    }

    @Override
    public LteFddStandardKpi findByKpiName(String kpiName) {
        return lteFddStandardKpiRepository
                .findByKpiName(kpiName)
                .orElseThrow(() -> new RuntimeException("Standard KPI not found by: " + kpiName));
    }

    @Override
    public LteFddStandardKpi findByKpiLabel(String kpiLabel) {
        return lteFddStandardKpiRepository
                .findByLabel(kpiLabel)
                .orElseThrow(()-> new RuntimeException("Standard KPI not found by: " + kpiLabel));
    }

    @Override
    public List<StandardKpiDto> getAllStandardKpi() {
        List<LteFddStandardKpi> standardKpis = lteFddStandardKpiRepository.findAllStandardKpi();
        return standardKpis.stream().map(mapper::lteFddStandardKpiToDto).toList();
    }
}
