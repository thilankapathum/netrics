package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddKpiDayRepository;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LteFddKpiDayServiceImpl implements LteFddKpiDayService {
    private final LteFddKpiDayRepository lteFddKpiDayRepository;
    private final Mapper mapper;

    @Override
    public List<KpiDataDto> findAll() {
        List<LteFddKpiDay> lteFddKpiDays = lteFddKpiDayRepository.findAll();

        List<KpiDataDto> kpiDataDtos = lteFddKpiDays
                .stream()
                .map(lfkd -> mapper.LteFddKpiDayToKpiDataDto(lfkd) )
                .toList();

        return kpiDataDtos;
    }

    @Override
    public List<KpiDataFractionDto> findAllWithFractions() {
        List<LteFddKpiDay> lteFddKpiDays = lteFddKpiDayRepository.findAll();

        List<KpiDataFractionDto> dtos = lteFddKpiDays
                .stream()
                .map(lfkd -> mapper.lteFddKpiDayToKpiDataFractionDto(lfkd) )
                .toList();

        return dtos;
    }
}
