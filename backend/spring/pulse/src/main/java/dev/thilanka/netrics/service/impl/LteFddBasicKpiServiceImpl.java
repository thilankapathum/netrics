package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.BasicKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.service.LteFddBasicKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddBasicKpiServiceImpl implements LteFddBasicKpiService {
    private final LteFddBasicKpiRepository lteFddBasicKpiRepository;
    private final RatService ratService;
    private final Mapper mapper;

    @Override
    public List<BasicKpiDto> getAllByRat(String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        List<BasicKpi> kpis = lteFddBasicKpiRepository.findByRatId(rat.getId());

        return kpis
                .stream()
                .map(mapper::lteFddBasicKpiToDto)
                .toList();
    }

    @Override
    public BasicKpiDto createBasicKpi(BasicKpiDto dto) {
        BasicKpi kpi = mapper.toLteFddBasicKpi(dto);
        BasicKpi savedKpi = lteFddBasicKpiRepository.save(kpi);
        return mapper.lteFddBasicKpiToDto(savedKpi);
    }

    @Override
    public BasicKpi findByKpiName(String kpiName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        BasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(kpiName, rat.getId())
                .orElseThrow(()-> new RuntimeException("Basic KPI not found by: " + kpiName));
        return basicKpi;
    }
}
