package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.entity.ltefdd.BasicKpi;
import dev.thilanka.netrics.entity.ltefdd.StandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.StandardKpiRepository;
import dev.thilanka.netrics.service.BasicKpiService;
import dev.thilanka.netrics.service.StandardKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StandardKpiServiceImpl implements StandardKpiService {
    private final StandardKpiRepository standardKpiRepository;
    private final BasicKpiService basicKpiService;
    private final RatService ratService;
    private final Mapper mapper;

    @Override
    public List<StandardKpiDto> getAll() {
        List<StandardKpi> kpis = standardKpiRepository.findAll();

        return kpis
                .stream()
                .map(mapper::lteFddStandardKpiToDto)
                .toList();
    }

    @Override
    public StandardKpiDto createKpi(StandardKpiDto kpiDto) {

        StandardKpi kpi = mapper.toLteFddStandardKpi(kpiDto);
        Rat rat = ratService.findRatByName(kpiDto.ratName());

        if (!Objects.equals(kpiDto.basicKpi(), "")) {
            System.out.println("Basic KPI: " + kpiDto.basicKpi());
            BasicKpi basicKpi = basicKpiService.findByKpiName(kpiDto.basicKpi(), kpiDto.ratName());
            kpi.setBasicKpi(basicKpi);
        }

        StandardKpi savedKpi = standardKpiRepository.save(kpi);
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
    public StandardKpi findByKpiName(String kpiName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);

        return standardKpiRepository
                .findByKpiNameAndRatId(kpiName, rat.getId())
                .orElseThrow(() -> new RuntimeException("Standard KPI not found by: " + kpiName));
    }

    @Override
    public StandardKpi findByKpiNameAndRatId(String kpiName, Long ratId) {
        return standardKpiRepository
                .findByKpiNameAndRatId(kpiName, ratId)
                .orElseThrow(() -> new RuntimeException("Standard KPI not found by: " + kpiName));
    }

    @Override
    public StandardKpi findByKpiLabel(String kpiLabel, String ratName) {
        return standardKpiRepository
                .findByLabel(kpiLabel)
                .orElseThrow(()-> new RuntimeException("Standard KPI not found by: " + kpiLabel));
    }

    @Override
    public List<StandardKpiDto> getAllStandardKpiByRat(String ratName) {
        Rat rat = ratService.findRatByName(ratName);

        List<StandardKpi> standardKpis = standardKpiRepository.findAllStandardKpiByRat(rat.getId());
        return standardKpis.stream().map(mapper::lteFddStandardKpiToDto).toList();
    }
}
