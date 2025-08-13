package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDay;
import dev.thilanka.netrics.entity.ltefdd.LteFddKpiDaySnap;
import dev.thilanka.netrics.entity.ltefdd.LteFddStandardKpi;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddKpiDayRepository;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LteFddKpiDayServiceImpl implements LteFddKpiDayService {
    private final LteFddKpiDayRepository lteFddKpiDayRepository;
    private final LteFddStandardKpiService lteFddStandardKpiService;
    private final Mapper mapper;

    @Override
    public List<KpiDataDto> findAll() {
        List<LteFddKpiDay> lteFddKpiDays = lteFddKpiDayRepository.findAll();

        return lteFddKpiDays
                .stream()
                .map(mapper::LteFddKpiDayToKpiDataDto)
                .toList();
    }

    @Override
    public List<KpiDataFractionDto> findAllWithFractions() {
        List<LteFddKpiDay> lteFddKpiDays = lteFddKpiDayRepository.findAll();

        return lteFddKpiDays
                .stream()
                .map(mapper::lteFddKpiDayToKpiDataFractionDto)
                .toList();
    }

    @Override
    public List<LteFddKpiDaySnap> getAverage() {
        return lteFddKpiDayRepository.getKpiX();
    }

    @Override
    public LteFddKpiDaySnap[] getKpiSnapshot(String standardKpiName, String period) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);
        System.out.println("KPI Name: " + standardKpi.getKpiName());

        LteFddKpiDaySnap kpiDaySnap = new LteFddKpiDaySnap();
        LteFddKpiDaySnap[] kpiDaySnaps = new LteFddKpiDaySnap[2];
        LocalDateTime timestamp = getLatestDate();

        if (Objects.equals(period, "day")) {
            kpiDaySnaps[0] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp, 0L)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            kpiDaySnaps[1] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp.minusDays(1),0L)
                    .orElseThrow(()-> new RuntimeException("Cannot retrieve KPI values"));
        } else if (Objects.equals(period, "week")) {
            kpiDaySnaps[0] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp, 6L)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            kpiDaySnaps[1] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp.minusDays(7), 6L)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        } else if (Objects.equals(period, "month")) {
            kpiDaySnaps[0] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp, 29L)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            kpiDaySnaps[1] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp.minusDays(30), 29L)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        } else {
            kpiDaySnaps = null;
        }

        return kpiDaySnaps;
    }

    private LocalDateTime getLatestDate() {
        LocalDateTime latestDate = lteFddKpiDayRepository.getLatestDate();
        System.out.println(latestDate);
        return latestDate;
    }

}
