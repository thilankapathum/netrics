package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.ltefdd.*;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.repository.LteFddKpiDayRepository;
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
    private final Mapper mapper;

    private static boolean checkImproved(String worstOrder, Double difference) {
        if (Objects.equals(worstOrder, "ASC")) {
            return difference > 0;
        } else if (Objects.equals(worstOrder, "DESC")) {
            return difference < 0;
        }
        return false;
    }

    //------------------------------- KPI-SNAPSHOT START ---------------------------------------------------------------


    private KpiSnapshotCurrentPre getLatestKpiSnapshotWithPre(LteFddStandardKpi standardKpi, String period) {

        //-- GET KPI WITH LABEL, WORST-ORDER, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

        LocalDateTime timestamp = lteFddKpiDayRepository.getLatestDate();

        KpiSnapshotCurrentPre kpiData = new KpiSnapshotCurrentPre();

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

    private KpiSnapshot getLatestKpiSnapshot(String kpiName, String period) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        KpiSnapshotCurrentPre kpiData = getLatestKpiSnapshotWithPre(standardKpi, period);

        KpiSnapshot snapshot = new KpiSnapshot();

        snapshot.setKpiLabel(standardKpi.getLabel());
//        snapshot.setBasic(false);

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
    public BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period) {

        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(basicKpiName)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));

        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (LteFddStandardKpi standardKpi : basicKpi.getLteFddStandardKpis()) {
            KpiSnapshot snapshot = getLatestKpiSnapshot(standardKpi.getKpiName(), period);
            kpiSnapshots.add(snapshot);
        }
        basicKpiSnapshot.setStandardKpis(kpiSnapshots);

        //-- CREATE BASIC-KPI'S DATA
//        KpiSnapshot basicKpiSnapshot = new KpiSnapshot();
//        basicKpiSnapshot.setBasic(true);
        basicKpiSnapshot.setKpiLabel(basicKpi.getLabel());

        basicKpiSnapshot.setPreviousValue(1.0);

        if (Objects.equals(basicKpi.getAggregation(), "MULTIPLY")) {

            basicKpiSnapshot.setValue(1.0);

            //-- Calculate Value & Pre-Value by multiplying component Standard-KPI values. [IMPORTANT: Assume component Standard-KPI are percentages]
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * snapshot.getValue() / 100.0);
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * snapshot.getPreviousValue() / 100.0);
            }
            basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * 100.0); //-- To avoid presenting decimals as percentages
            basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * 100.0); //-- To avoid presenting decimals as percentages

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.getWorstOrder(), basicKpiSnapshot.getDifference()));

//            kpiSnapshots.add(basicKpiSnapshot);
        } else if (Objects.equals(basicKpi.getAggregation(), "SUM")){

            basicKpiSnapshot.setValue(0.0);

            //-- Calculate Value & Pre-Value by adding component Standard-KPI values.
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() + snapshot.getValue());
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() + snapshot.getPreviousValue());
            }

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.getWorstOrder(), basicKpiSnapshot.getDifference()));

//            kpiSnapshots.add(basicKpiSnapshot);
        } else {
            basicKpiSnapshot.setValue(null);
            basicKpiSnapshot.setPreviousValue(null);
            basicKpiSnapshot.setDifference(null);
            basicKpiSnapshot.setImproved(false);

//            kpiSnapshots.add(basicKpiSnapshot);
        }

        return basicKpiSnapshot;
    }


    //------------------------------- KPI-SNAPSHOT END -----------------------------------------------------------------

    // ------------------------------ WORST-CELLS START ----------------------------------------------------------------

    private List<WorstCellCurrentPre> getWorstCellsByKpiWithPre(String basicKpiName, String period, int count) {

        //-- GET WORST CELLS WITH CELL-NAME, LABEL, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(basicKpiName);
        LocalDateTime timestamp = getLatestDate();

        List<WorstCellCurrentPre> worstCells = new ArrayList<>();  //-- Get KpiValue and CalculatedKpiValue for each worst cell

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

        for (WorstCellCurrentPre worstCell : worstCells) {
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
    public List<WorstCell> getWorstCellsByKpi(String kpiName, String period, int count) {

        //-- GET WORST CELLS WITH CELL-NAME, LABEL, VALUE, PRE-VALUE, DIFFERENCE, IMPROVED

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
//        List<KpiSnapshot> finalKpiSnapshots = new ArrayList<>();
        List<WorstCell> finalWorstCells = new ArrayList<>();
        List<WorstCellCurrentPre> worstCells = getWorstCellsByKpiWithPre(kpiName, period, count);

        for (WorstCellCurrentPre worstCell : worstCells) {

            WorstCell finalWorstCell = new WorstCell();

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


    // ------------------------------ WORST-CELLS END ------------------------------------------------------------------


    // ------------------------------ CELL KPI - START -----------------------------------------------------------------

    @Override
    public List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period) {

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);
        LocalDateTime timestamp = getLatestDate();
        List<KpiData> kpiData = new ArrayList<>();

        switch (period) {
            case "day" -> {
                kpiData = lteFddKpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, 0L, cellName);
            }
            case "week" -> {
                kpiData = lteFddKpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, 6L, cellName);
            }
            case "month" -> {
                kpiData = lteFddKpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, 29L, cellName);
            }
            case "quarter" -> {
                kpiData = lteFddKpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, 89L, cellName);
            }
            case null, default -> {
                kpiData = lteFddKpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, 30L, cellName);
            }
        }

        return kpiData.stream()
                .map(mapper::kpiDataToDto)
                .toList();
    }



    // ------------------------------ CELL KPI - END -------------------------------------------------------------------

    // ------------------------------ KPI TREND - END -------------------------------------------------------------------

    @Override
    public List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);
        LocalDateTime timestamp = getLatestDate();
        List<KpiTrend> kpiTrends = new ArrayList<>();

        switch (period) {
            case "week" ->{
                if (Objects.equals(standardKpi.getAggregation(), "SUM")){
                    kpiTrends = lteFddKpiDayRepository.findTrendDataSumByKpi(standardKpi.getId(), timestamp, 6L);
                } else kpiTrends = lteFddKpiDayRepository.findTrendDataAvgByKpi(standardKpi.getId(), timestamp, 6L);
            }
            case "month" -> {
                if (Objects.equals(standardKpi.getAggregation(), "SUM")){
                    kpiTrends = lteFddKpiDayRepository.findTrendDataSumByKpi(standardKpi.getId(), timestamp, 29L);
                } else kpiTrends = lteFddKpiDayRepository.findTrendDataAvgByKpi(standardKpi.getId(), timestamp, 29L);
            }
            case "quarter" ->{
                if (Objects.equals(standardKpi.getAggregation(), "SUM")){
                    kpiTrends = lteFddKpiDayRepository.findTrendDataSumByKpi(standardKpi.getId(), timestamp, 89L);
                } else kpiTrends = lteFddKpiDayRepository.findTrendDataAvgByKpi(standardKpi.getId(), timestamp, 89L);
            }
        }
        return kpiTrends.stream().map(mapper::kpiTrendToDto).toList();
    }


    // ------------------------------ KPI TREND - END -------------------------------------------------------------------


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
