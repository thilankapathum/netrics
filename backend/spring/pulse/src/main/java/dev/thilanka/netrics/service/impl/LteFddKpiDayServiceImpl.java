package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.FinalKpiSnapshot;
import dev.thilanka.netrics.entity.KpiSnapshot;
import dev.thilanka.netrics.entity.WorstCellKpiData;
import dev.thilanka.netrics.entity.WorstCellKpiDataCurrPre;
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
                if (standardKpi.getAggregation().equals("SUM")) {
                    kpiSnapshot = lteFddKpiDayRepository
                            .findLatestCalculatedSumKpiSnapshot(standardKpi.getId(), timestamp, 0L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                } else {
                    kpiSnapshot = lteFddKpiDayRepository
                            .findLatestCalculatedAvgKpiSnapshot(standardKpi.getId(), timestamp, 0L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                }

            }
            case "week" -> {
                if (isPrevious) timestamp = timestamp.minusDays(7);
                if (standardKpi.getAggregation().equals("SUM")) {
                    kpiSnapshot = lteFddKpiDayRepository
                            .findLatestCalculatedSumKpiSnapshot(standardKpi.getId(), timestamp, 6L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                } else {
                    kpiSnapshot = lteFddKpiDayRepository
                            .findLatestCalculatedAvgKpiSnapshot(standardKpi.getId(), timestamp, 6L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                }

            }
            case "month" -> {
                if (isPrevious) timestamp = timestamp.minusDays(30);
                if (standardKpi.getAggregation().equals("SUM")) {
                    kpiSnapshot = lteFddKpiDayRepository
                            .findLatestCalculatedSumKpiSnapshot(standardKpi.getId(), timestamp, 29L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                } else {
                    kpiSnapshot = lteFddKpiDayRepository
                            .findLatestCalculatedAvgKpiSnapshot(standardKpi.getId(), timestamp, 29L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                }

            }
        }
        if (kpiSnapshot.getKpiValue() == null && kpiSnapshot.getCalculatedKpiValue() == null) {
            kpiSnapshot.setKpiValue(0.0);
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
            snapshot.setValue(kpiSnapshotWithPrevious[0].getKpiValue());
            snapshot.setPreviousValue(kpiSnapshotWithPrevious[1].getKpiValue());
            snapshot.setDifference(kpiSnapshotWithPrevious[0].getKpiValue() - kpiSnapshotWithPrevious[1].getKpiValue());
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
            return difference > 0;
        } else if (Objects.equals(worstOrder, "DESC")) {
            return difference < 0;
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

    // ------------------------------ WORST CELLS START ----------------------------------------------------------------

    @Override
    public List<WorstCellKpiData> findWorstCellsByKpi(String basicKpiName, String period, int count) {

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(basicKpiName);
        LocalDateTime timestamp = getLatestDate();

        List<WorstCellKpiData> worstCells = new ArrayList<>();  //-- Get KpiValue and CalculatedKpiValue for each worst cell

        switch (period) {
            case "day" -> {
                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumAsc(standardKpi.getId(), timestamp, 0L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgAsc(standardKpi.getId(), timestamp, 0L, count);
                    }
                } else {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumDesc(standardKpi.getId(), timestamp, 0L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgDesc(standardKpi.getId(), timestamp, 0L, count);
                    }
                }
            }
            case "week" -> {
                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumAsc(standardKpi.getId(), timestamp, 6L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgAsc(standardKpi.getId(), timestamp, 6L, count);
                    }
                } else {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumDesc(standardKpi.getId(), timestamp, 6L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgDesc(standardKpi.getId(), timestamp, 6L, count);
                    }
                }
            }
            case "month" -> {
                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumAsc(standardKpi.getId(), timestamp, 29L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgAsc(standardKpi.getId(), timestamp, 29L, count);
                    }
                } else {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumDesc(standardKpi.getId(), timestamp, 29L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgDesc(standardKpi.getId(), timestamp, 29L, count);
                    }
                }
            }
        }
        return worstCells;
    }


    // ------------------------------ WORST CELLS END ----------------------------------------------------------------

    //  -------------------------- WORST CELLS WITH PREV - START -------------------------------------------------------


    @Override
    public List<WorstCellKpiDataCurrPre> findWorstCellsByKpiWithPre(String basicKpiName, String period, int count) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(basicKpiName);
        LocalDateTime timestamp = getLatestDate();

        List<WorstCellKpiDataCurrPre> worstCells = new ArrayList<>();  //-- Get KpiValue and CalculatedKpiValue for each worst cell

        switch (period) {
            case "day" -> {
                LocalDateTime preTimestamp = timestamp.minusDays(1);
                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevSumAsc(standardKpi.getId(), timestamp, preTimestamp, 0L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevAvgAsc(standardKpi.getId(), timestamp, preTimestamp, 0L, count);
                    }
                } else {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevSumDesc(standardKpi.getId(), timestamp, preTimestamp, 0L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevAvgDesc(standardKpi.getId(), timestamp, preTimestamp, 0L, count);
                    }
                }
            }
            case "week" -> {
                LocalDateTime preTimestamp = timestamp.minusDays(7);
                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevSumAsc(standardKpi.getId(), timestamp, preTimestamp, 6L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevAvgAsc(standardKpi.getId(), timestamp, preTimestamp, 6L, count);
                    }
                } else {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevSumDesc(standardKpi.getId(), timestamp, preTimestamp, 6L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevAvgDesc(standardKpi.getId(), timestamp, preTimestamp, 6L, count);
                    }
                }
            }
            case "month" -> {
                LocalDateTime preTimestamp = timestamp.minusDays(30);
                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevSumAsc(standardKpi.getId(), timestamp, preTimestamp, 29L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevAvgAsc(standardKpi.getId(), timestamp, preTimestamp, 29L, count);
                    }
                } else {
                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevSumDesc(standardKpi.getId(), timestamp, preTimestamp, 29L, count);
                    } else {
                        worstCells = lteFddKpiDayRepository.findWorstCellsWithPrevAvgDesc(standardKpi.getId(), timestamp, preTimestamp, 29L, count);
                    }
                }
            }
        }

        for (WorstCellKpiDataCurrPre worstCell : worstCells) {
            if (worstCell.getKpiValue() == null) {
                worstCell.setKpiValue(0.0);
            }
            if (worstCell.getPreKpiValue() == null) {
                worstCell.setPreKpiValue(0.0);
            }
        }

        return worstCells;
    }

    @Override
    public List<FinalKpiSnapshot> getFinalWorstCellsByKpi(String kpiName, String period, int count, boolean isBasic) {

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        List<FinalKpiSnapshot> finalKpiSnapshots = new ArrayList<>();
        List<WorstCellKpiDataCurrPre> worstCells = findWorstCellsByKpiWithPre(kpiName, period, count);

        for (WorstCellKpiDataCurrPre worstCell : worstCells) {
            FinalKpiSnapshot finalKpiSnapshot = new FinalKpiSnapshot();

            finalKpiSnapshot.setKpiLabel(worstCell.getLabel());
            finalKpiSnapshot.setValue(worstCell.getKpiValue());
            finalKpiSnapshot.setBasic(isBasic);

            if (worstCell.getCalculatedKpiValue() == null) {
                finalKpiSnapshot.setValue(worstCell.getKpiValue());
                finalKpiSnapshot.setPreviousValue(worstCell.getPreKpiValue());
            } else {
                finalKpiSnapshot.setValue(worstCell.getCalculatedKpiValue());
                finalKpiSnapshot.setPreviousValue(worstCell.getPreCalculatedKpiValue());
            }

            if (finalKpiSnapshot.getValue() == null) finalKpiSnapshot.setValue(0.0);
            if (finalKpiSnapshot.getPreviousValue() == null) finalKpiSnapshot.setPreviousValue(0.0);

            finalKpiSnapshot.setDifference(finalKpiSnapshot.getValue() - finalKpiSnapshot.getPreviousValue());
            finalKpiSnapshot.setImproved(checkImproved(standardKpi.getWorstOrder(), finalKpiSnapshot.getDifference()));

            finalKpiSnapshots.add(finalKpiSnapshot);
        }
        return finalKpiSnapshots;
    }

    //  -------------------------- WORST CELLS WITH PREV - END ---------------------------------------------------------


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
