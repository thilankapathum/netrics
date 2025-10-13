package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.service.LteFddBasicKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LteFddBasicKpiServiceImpl implements LteFddBasicKpiService {
    private final LteFddBasicKpiRepository lteFddBasicKpiRepository;
    private final RatService ratService;
    private final Mapper mapper;

    @Override
    public List<BasicKpiDto> getAllByRat(String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        List<LteFddBasicKpi> kpis = lteFddBasicKpiRepository.findByRatId(rat.getId());

        return kpis
                .stream()
                .map(mapper::lteFddBasicKpiToDto)
                .toList();
    }

    @Override
    public BasicKpiDto createBasicKpi(BasicKpiDto dto) {
        LteFddBasicKpi kpi = mapper.toLteFddBasicKpi(dto);
        LteFddBasicKpi savedKpi = lteFddBasicKpiRepository.save(kpi);
        return mapper.lteFddBasicKpiToDto(savedKpi);
    }

    @Override
    public LteFddBasicKpi findByKpiName(String kpiName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(kpiName, rat.getId())
                .orElseThrow(()-> new RuntimeException("Basic KPI not found by: " + kpiName));
        return basicKpi;
    }
}
