package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.LteFddBasicKpiDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.service.LteFddBasicKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddBasicKpiServiceImpl implements LteFddBasicKpiService {
    private final LteFddBasicKpiRepository lteFddBasicKpiRepository;
    private final Mapper mapper;

    @Override
    public List<LteFddBasicKpiDto> getAll() {
        List<LteFddBasicKpi> kpis = lteFddBasicKpiRepository.findAll();

        return kpis
                .stream()
                .map(mapper::lteFddBasicKpiToDto)
                .toList();
    }

    @Override
    public LteFddBasicKpiDto createBasicKpi(LteFddBasicKpiDto dto) {
        LteFddBasicKpi kpi = mapper.toLteFddBasicKpi(dto);
        LteFddBasicKpi savedKpi = lteFddBasicKpiRepository.save(kpi);
        return mapper.lteFddBasicKpiToDto(savedKpi);
    }
}
