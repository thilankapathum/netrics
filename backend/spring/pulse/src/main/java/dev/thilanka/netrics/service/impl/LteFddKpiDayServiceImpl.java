package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.entity.*;
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


    private KpiDataCurrPre getLatestKpiSnapshotWithPre(LteFddStandardKpi standardKpi, String period) {

        //-- GET KPI WITH LABEL, WORST-ORDER, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

//        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        LocalDateTime timestamp = lteFddKpiDayRepository.getLatestDate();

        KpiDataCurrPre kpiData = new KpiDataCurrPre();

        switch (period) {
            case "day" -> {
                LocalDateTime preTimestamp = timestamp.minusDays(1L);
                if (standardKpi.getAggregation().equals("SUM")) {
                    kpiData = lteFddKpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, 0L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                } else {
                    kpiData = lteFddKpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, 0L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                }
            }
            case "week" -> {
                LocalDateTime preTimestamp = timestamp.minusDays(7L);
                if (standardKpi.getAggregation().equals("SUM")) {
                    kpiData = lteFddKpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, 6L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                } else {
                    kpiData = lteFddKpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, 6L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                }
            }
            case "month" -> {
                LocalDateTime preTimestamp = timestamp.minusDays(30L);
                if (standardKpi.getAggregation().equals("SUM")) {
                    kpiData = lteFddKpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, 29L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                } else {
                    kpiData = lteFddKpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, 29L)
                            .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                }
            }
        }
        return kpiData;
    }

    private FinalKpiSnapshot getLatestKpiSnapshot(String kpiName, String period) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        KpiDataCurrPre kpiData = getLatestKpiSnapshotWithPre(standardKpi, period);

        FinalKpiSnapshot snapshot = new FinalKpiSnapshot();

        snapshot.setKpiLabel(standardKpi.getLabel());
        snapshot.setBasic(false);

        if (kpiData.getCalculatedKpiValue() == null) {   //-- CalculatedValue == null -> Value should be taken from kpiValue
            snapshot.setValue(kpiData.getKpiValue());
            if (kpiData.getPreKpiValue() == null) kpiData.setPreKpiValue(0.0);
            snapshot.setPreviousValue(kpiData.getPreKpiValue());
        } else {
            if (Objects.equals(standardKpi.getUnit(), "%")) {
                snapshot.setValue(kpiData.getCalculatedKpiValue() * 100.0);
                if (kpiData.getPreCalculatedKpiValue() == null) kpiData.setPreCalculatedKpiValue(0.0);
                snapshot.setPreviousValue(kpiData.getPreCalculatedKpiValue() * 100.0);
            } else {
                snapshot.setValue(kpiData.getCalculatedKpiValue());
                if (kpiData.getPreCalculatedKpiValue() == null) kpiData.setPreCalculatedKpiValue(0.0);
                snapshot.setPreviousValue(kpiData.getPreCalculatedKpiValue());
            }
        }

        snapshot.setDifference(snapshot.getValue() - snapshot.getPreviousValue());

        snapshot.setImproved(checkImproved(standardKpi.getWorstOrder(), snapshot.getDifference()));

        return snapshot;
    }


    @Override
    public List<FinalKpiSnapshot> getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period) {

        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(basicKpiName)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));

        List<FinalKpiSnapshot> finalKpiSnapshots = new ArrayList<>();

        for (LteFddStandardKpi standardKpi : basicKpi.getLteFddStandardKpis()) {
            FinalKpiSnapshot snapshot = getLatestKpiSnapshot(standardKpi.getKpiName(), period);
            finalKpiSnapshots.add(snapshot);
        }

        //-- CREATE BASIC-KPI'S DATA
        FinalKpiSnapshot basicKpiSnapshot = new FinalKpiSnapshot();
        basicKpiSnapshot.setBasic(true);
        basicKpiSnapshot.setKpiLabel(basicKpi.getLabel());
        basicKpiSnapshot.setValue(1.0);
        basicKpiSnapshot.setPreviousValue(1.0);
        //-- Calculate Value & Pre-Value by multiplying component Standard-KPI values. [IMPORTANT: Assume component Standard-KPI are percentages]
        for (FinalKpiSnapshot snapshot : finalKpiSnapshots) {
            basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * snapshot.getValue() / 100.0);
            basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * snapshot.getPreviousValue() / 100.0);
        }
        basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * 100.0); //-- To avoid presenting decimals as percentages
        basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * 100.0); //-- To avoid presenting decimals as percentages

        basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
        basicKpiSnapshot.setImproved(checkImproved(basicKpi.getWorstOrder(), basicKpiSnapshot.getDifference()));

        finalKpiSnapshots.add(basicKpiSnapshot);
        return finalKpiSnapshots;
    }


    // ------------------------------ WORST CELLS START ----------------------------------------------------------------

//    @Override
//    public List<WorstCellKpiData> findWorstCellsByKpi(String basicKpiName, String period, int count) {
//
//        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(basicKpiName);
//        LocalDateTime timestamp = getLatestDate();
//
//        List<WorstCellKpiData> worstCells = new ArrayList<>();  //-- Get KpiValue and CalculatedKpiValue for each worst cell
//
//        switch (period) {
//            case "day" -> {
//                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
//                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumAsc(standardKpi.getId(), timestamp, 0L, count);
//                    } else {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgAsc(standardKpi.getId(), timestamp, 0L, count);
//                    }
//                } else {
//                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumDesc(standardKpi.getId(), timestamp, 0L, count);
//                    } else {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgDesc(standardKpi.getId(), timestamp, 0L, count);
//                    }
//                }
//            }
//            case "week" -> {
//                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
//                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumAsc(standardKpi.getId(), timestamp, 6L, count);
//                    } else {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgAsc(standardKpi.getId(), timestamp, 6L, count);
//                    }
//                } else {
//                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumDesc(standardKpi.getId(), timestamp, 6L, count);
//                    } else {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgDesc(standardKpi.getId(), timestamp, 6L, count);
//                    }
//                }
//            }
//            case "month" -> {
//                if (Objects.equals(standardKpi.getWorstOrder(), "ASC")) {
//                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumAsc(standardKpi.getId(), timestamp, 29L, count);
//                    } else {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgAsc(standardKpi.getId(), timestamp, 29L, count);
//                    }
//                } else {
//                    if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiSumDesc(standardKpi.getId(), timestamp, 29L, count);
//                    } else {
//                        worstCells = lteFddKpiDayRepository.findWorstCellsByKpiAvgDesc(standardKpi.getId(), timestamp, 29L, count);
//                    }
//                }
//            }
//        }
//        return worstCells;
//    }


    // ------------------------------ WORST CELLS END ----------------------------------------------------------------

    //  -------------------------- WORST CELLS WITH PREV - START -------------------------------------------------------


    private List<WorstCellKpiDataCurrPre> getWorstCellsByKpiWithPre(String basicKpiName, String period, int count) {

        //-- GET WORST CELLS WITH CELL-NAME LABEL, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

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
    public List<FinalWorstCellData> getWorstCellsByKpi(String kpiName, String period, int count) {

        //-- GET WORST CELLS WITH CELL-NAME, LABEL, VALUE, PRE-VALUE, DIFFERENCE, IMPROVED & IS-BASIC

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
//        List<FinalKpiSnapshot> finalKpiSnapshots = new ArrayList<>();
        List<FinalWorstCellData> finalWorstCells = new ArrayList<>();
        List<WorstCellKpiDataCurrPre> worstCells = getWorstCellsByKpiWithPre(kpiName, period, count);

        for (WorstCellKpiDataCurrPre worstCell : worstCells) {

            FinalWorstCellData finalWorstCell = new FinalWorstCellData();

            finalWorstCell.setCellName(worstCell.getCellName());
            finalWorstCell.setKpiLabel(worstCell.getLabel());
            finalWorstCell.setValue(worstCell.getKpiValue());
//            finalWorstCell.setBasic(isBasic);

            if (worstCell.getCalculatedKpiValue() == null) {
                finalWorstCell.setValue(worstCell.getKpiValue());
                finalWorstCell.setPreviousValue(worstCell.getPreKpiValue());
            } else {

                if (Objects.equals(standardKpi.getUnit(), "%")) {
                    if (worstCell.getPreCalculatedKpiValue() == null) worstCell.setPreCalculatedKpiValue(0.0);
                    finalWorstCell.setValue(worstCell.getCalculatedKpiValue() * 100.0);     //-- Make value a percentage
                    finalWorstCell.setPreviousValue(worstCell.getPreCalculatedKpiValue() * 100.0);      //-- Make value a percentage
                } else {
                    if (worstCell.getPreCalculatedKpiValue() == null) worstCell.setPreCalculatedKpiValue(0.0);
                    finalWorstCell.setValue(worstCell.getCalculatedKpiValue());
                    finalWorstCell.setPreviousValue(worstCell.getPreCalculatedKpiValue());
                }
            }

            if (finalWorstCell.getValue() == null) finalWorstCell.setValue(0.0);
            if (finalWorstCell.getPreviousValue() == null) finalWorstCell.setPreviousValue(0.0);

            finalWorstCell.setDifference(finalWorstCell.getValue() - finalWorstCell.getPreviousValue());
            finalWorstCell.setImproved(checkImproved(standardKpi.getWorstOrder(), finalWorstCell.getDifference()));

            finalWorstCells.add(finalWorstCell);
        }
        return finalWorstCells;
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
