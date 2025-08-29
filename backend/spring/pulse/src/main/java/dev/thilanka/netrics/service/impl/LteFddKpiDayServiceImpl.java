package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiTrendDto;
import dev.thilanka.netrics.dto.WorstCellsDto;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.entity.ltefdd.*;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.LteFddBasicKpiRepository;
import dev.thilanka.netrics.repository.LteFddKpiDayRepository;
import dev.thilanka.netrics.service.DistrictService;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import dev.thilanka.netrics.service.LteFddStandardKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final DistrictService districtService;
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

        LocalDateTime preTimestamp = getLatestPreviousDate(period);
        if (standardKpi.getAggregation().equals("SUM")) {
            return lteFddKpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period))
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        } else {
            return lteFddKpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period))
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        }
    }

    private KpiSnapshot getLatestKpiSnapshot(String kpiName, String period) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        KpiSnapshotCurrentPre kpiData = getLatestKpiSnapshotWithPre(standardKpi, period);

        KpiSnapshot snapshot = new KpiSnapshot();

        snapshot.setKpiLabel(standardKpi.getLabel());
        snapshot.setUnit(standardKpi.getUnit());

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
        basicKpiSnapshot.setKpiLabel(basicKpi.getLabel());
        basicKpiSnapshot.setUnit(basicKpi.getUnit());

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

        } else if (Objects.equals(basicKpi.getAggregation(), "SUM")) {

            basicKpiSnapshot.setValue(0.0);

            //-- Calculate Value & Pre-Value by adding component Standard-KPI values.
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() + snapshot.getValue());
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() + snapshot.getPreviousValue());
            }

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.getWorstOrder(), basicKpiSnapshot.getDifference()));

        } else {
            basicKpiSnapshot.setValue(null);
            basicKpiSnapshot.setPreviousValue(null);
            basicKpiSnapshot.setDifference(null);
            basicKpiSnapshot.setImproved(false);
        }

        return basicKpiSnapshot;
    }


    //------------------------------- KPI-SNAPSHOT END -----------------------------------------------------------------

    // ------------------------------ WORST-CELLS START ----------------------------------------------------------------


    @Override
    public Page<WorstCellsDto> getWorstCellsByKpiPage(String kpiName, String period, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        LocalDateTime timestamp = getLatestDate();
        LocalDateTime preTimestamp = getLatestPreviousDate(period);

        return lteFddKpiDayRepository.findWorstCells(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), pageable);
    }

    @Override
    public Page<WorstCellsDto> getWorstCellsByKpiAndDistrictPage(String kpiName, String period, String districtName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(kpiName);
        District district = districtService.findDistrictByName(districtName);
        LocalDateTime timestamp = getLatestDate();
        LocalDateTime preTimestamp = getLatestPreviousDate(period);

        return lteFddKpiDayRepository.findWorstCellsByDistrict(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), pageable, district.getId());
    }




    // ------------------------------ WORST-CELLS END ------------------------------------------------------------------


    // ------------------------------ CELL KPI - START -----------------------------------------------------------------

    @Override
    public List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period) {

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);
        LocalDateTime timestamp = getLatestDate();

        List<KpiData> kpiData = lteFddKpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, getPeriod(period), cellName);

        return kpiData.stream()
                .map(mapper::kpiDataToDto)
                .toList();
    }

    @Override
    public List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period) {

        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiLabel(kpiLabel);

        return getDataByKpiAndCell(standardKpi.getKpiName(), cellName, period);
    }


    // ------------------------------ CELL KPI - END -------------------------------------------------------------------

    // ------------------------------ KPI TREND - END -------------------------------------------------------------------

    @Override
    public List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);
        LocalDateTime timestamp = getLatestDate();
        List<KpiTrend> kpiTrends = new ArrayList<>();

        if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
            kpiTrends = lteFddKpiDayRepository.findTrendDataSumByKpi(standardKpi.getId(), timestamp, getPeriod(period));
        } else kpiTrends = lteFddKpiDayRepository.findTrendDataAvgByKpi(standardKpi.getId(), timestamp, getPeriod(period));

        return kpiTrends.stream().map(mapper::kpiTrendToDto).toList();
    }

    @Override
    public List<KpiTrendDto> getTrendByKpiAndDistrict(String standardKpiName, String period, String districtName) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);
        District district = districtService.findDistrictByName(districtName);
        LocalDateTime timestamp = getLatestDate();
        List<KpiTrend> kpiTrends = new ArrayList<>();

        if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
            kpiTrends = lteFddKpiDayRepository.findTrendDataSumByKpiAndDistrict(standardKpi.getId(), timestamp, getPeriod(period), district.getId());
        } else kpiTrends = lteFddKpiDayRepository.findTrendDataAvgByKpiAndDistrict(standardKpi.getId(), timestamp, getPeriod(period), district.getId());

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

    @Override
    public KpiDataDto createLteFddKpiDay(LteFddKpiDay kpiDay) {
        LteFddKpiDay savedKpiDay = lteFddKpiDayRepository.save(kpiDay);
        return mapper.LteFddKpiDayToKpiDataDto(savedKpiDay);
    }

    private LocalDateTime getLatestDate() {
        return lteFddKpiDayRepository.getLatestDate();
    }

    private LocalDateTime getLatestPreviousDate(String period) {
        switch (period) {
            case "day" -> {
                return getLatestDate().minusDays(1);
            }
            case "week" -> {
                return getLatestDate().minusDays(7);
            }
            case "month" -> {
                return getLatestDate().minusDays(30);
            }
            case "quarter" -> {
                return getLatestDate().minusDays(90);
            }
            case "half-year" -> {
                return getLatestDate().minusDays(180);
            }
            case "year" -> {
                return getLatestDate().minusDays(365);
            }
        }
        return null;
    }

    private Long getPeriod(String period) {
        switch (period) {
            case "day" -> {
                return 0L;
            }
            case "week" -> {
                return 6L;
            }
            case "month" -> {
                return 29L;
            }
            case "quarter" -> {
                return 89L;
            }
            case "half-year" -> {
                return 179L;
            }
            case "year" -> {
                return 364L;
            }
        }
        return null;
    }

    @Override
    public List<LteFddKpiDay> getKpiWithoutDistrict() {
        return lteFddKpiDayRepository.findKpiWithoutDistrict();
    }

}
