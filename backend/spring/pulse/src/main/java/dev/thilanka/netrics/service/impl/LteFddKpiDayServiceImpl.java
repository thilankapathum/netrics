package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.FinalKpiSnapshot;
import dev.thilanka.netrics.entity.KpiSnapshot;
import dev.thilanka.netrics.entity.ltefdd.*;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.repository.LteFddKpiDayRepository;
import dev.thilanka.netrics.repository.LteFddStandardKpiRepository;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LteFddKpiDayServiceImpl implements LteFddKpiDayService {

    //-- Snapshot: Single whole KPI value considering the KPI and Period
    //-- Standard KPI: KPIs like 'E-RAB Setup Success Rate', 'DL Volume (Kbyte)'
    //-- Basic KPI: KPIs like 'Accessibility', 'Retainability'
    //-- Latest: Last day (newest) KPI
    //-- Compact: Contains only 'kpiLabel', 'value', 'difference with previous period', 'up/down with previous period'

    private final LteFddKpiDayRepository lteFddKpiDayRepository;
    private final LteFddStandardKpiService lteFddStandardKpiService;
    private final LteFddBasicKpiRepository lteFddBasicKpiRepository;
    private final LteFddStandardKpiRepository lteFddStandardKpiRepository;
    private final Mapper mapper;

    //------------------------------- CALCULATED START -----------------------------------------------------------------

    @Override
    public KpiSnapshot getLatestCalculatedKpiSnapshot(String kpiName, String period, boolean isPrevious) {
        LocalDateTime timestamp = getLatestDate();
        KpiSnapshot kpiSnapshot = new KpiSnapshot();
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);

        switch (period) {
            case "day" -> {
                if (isPrevious) timestamp = timestamp.minusDays(1);
                kpiSnapshot = lteFddKpiDayRepository
                        .findLatestCalculatedKpiSnapshot(standardKpi.getId(), timestamp, 0L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case "week" -> {
                if (isPrevious) timestamp = timestamp.minusDays(7);
                kpiSnapshot = lteFddKpiDayRepository
                        .findLatestCalculatedKpiSnapshot(standardKpi.getId(), timestamp, 6L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case "month" -> {
                if (isPrevious) timestamp = timestamp.minusDays(30);
                kpiSnapshot = lteFddKpiDayRepository
                        .findLatestCalculatedKpiSnapshot(standardKpi.getId(), timestamp, 29L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
        }
        if (kpiSnapshot.getKpiValueSum() == null && kpiSnapshot.getCalculatedKpiValue() == null) {
            kpiSnapshot.setKpiValueSum(0.0);
            kpiSnapshot.setCalculatedKpiValue(0.0);
        }
        return kpiSnapshot;
    }


    private KpiSnapshot[] getLatestCalculatedKpiSnapshotWithPrevious(String kpiName, String period) {
        KpiSnapshot[] kpiSnapshotWithPrevious = new KpiSnapshot[2];

        kpiSnapshotWithPrevious[0] = getLatestCalculatedKpiSnapshot(kpiName, period, false);
        kpiSnapshotWithPrevious[1] = getLatestCalculatedKpiSnapshot(kpiName, period, true);   //-- Array [1] will hold previous period KPI

        return kpiSnapshotWithPrevious;
    }

    private FinalKpiSnapshot getLatestCompactCalculatedKpiSnapshot(String kpiName, String period, boolean isBasic) {
        KpiSnapshot[] kpiSnapshotWithPrevious = getLatestCalculatedKpiSnapshotWithPrevious(kpiName, period);

        FinalKpiSnapshot snapshot = new FinalKpiSnapshot();

        snapshot.setKpiLabel(kpiSnapshotWithPrevious[0].getLabel());
        snapshot.setBasic(isBasic);

        if (kpiSnapshotWithPrevious[0].getCalculatedKpiValue() == null) {
            snapshot.setValue(kpiSnapshotWithPrevious[0].getKpiValueSum());
            snapshot.setPreviousValue(kpiSnapshotWithPrevious[1].getKpiValueSum());
            snapshot.setDifference(kpiSnapshotWithPrevious[0].getKpiValueSum() - kpiSnapshotWithPrevious[1].getKpiValueSum());
        } else {
            snapshot.setValue(kpiSnapshotWithPrevious[0].getCalculatedKpiValue());
            snapshot.setPreviousValue(kpiSnapshotWithPrevious[1].getCalculatedKpiValue());
            snapshot.setDifference(kpiSnapshotWithPrevious[0].getCalculatedKpiValue() - kpiSnapshotWithPrevious[1].getCalculatedKpiValue());
        }

        snapshot.setImproved(checkImproved(kpiSnapshotWithPrevious[0].getWorstOrder(), snapshot.getDifference()));

        return snapshot;
    }

    private static boolean checkImproved(String worstOrder, Double difference) {
        if (Objects.equals(worstOrder, "ASC")) {
            if (difference > 0) {
                return true;
            } else {
                return false;
            }
        } else if (Objects.equals(worstOrder, "DESC")) {
            if (difference < 0) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


    @Override
    public List<FinalKpiSnapshot> getLatestBasicAndStandardKpiSnapshot(String basicKpiName, String period) {

        List<FinalKpiSnapshot> kpiSnapshotList = new ArrayList<>(); //-- Hold Snapshots of Basic KPI & its component Standard KPIs

        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(basicKpiName)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));

        FinalKpiSnapshot basicKpiSnapshot = new FinalKpiSnapshot(); //-- Basic KPI values
        basicKpiSnapshot.setKpiLabel(basicKpi.getLabel());
        basicKpiSnapshot.setBasic(true);
        basicKpiSnapshot.setValue(1.0);
        basicKpiSnapshot.setPreviousValue(1.0);

        //-- Get component basic KPI and add to kpiSnapshotList
        for (LteFddStandardKpi standardKpi : basicKpi.getLteFddStandardKpis()) {
//            standardKpi.getWorstOrder()
            kpiSnapshotList.add(getLatestCompactCalculatedKpiSnapshot(standardKpi.getKpiName(), period, false));
        }

        //-- Set Multiplication of Standard KPI's values to Basic KPI value
        for (FinalKpiSnapshot snapshot : kpiSnapshotList) {
            basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * snapshot.getValue());
            basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * snapshot.getPreviousValue());
        }

        basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());

        basicKpiSnapshot.setImproved(checkImproved(basicKpi.getWorstOrder(), basicKpiSnapshot.getDifference()));

        kpiSnapshotList.add(basicKpiSnapshot);

        return kpiSnapshotList;
    }


    //------------------------------- CALCULATED END -------------------------------------------------------------------

    @Override
    public List<KpiDataDto> findAll() {
        List<LteFddKpiDay> lteFddKpiDays = lteFddKpiDayRepository.findAll();

        return lteFddKpiDays
                .stream()
                .map(mapper::LteFddKpiDayToKpiDataDto)
                .toList();
    }

    private LocalDateTime getLatestDate() {
        LocalDateTime latestDate = lteFddKpiDayRepository.getLatestDate();
        System.out.println(latestDate);
        return latestDate;
    }

}
