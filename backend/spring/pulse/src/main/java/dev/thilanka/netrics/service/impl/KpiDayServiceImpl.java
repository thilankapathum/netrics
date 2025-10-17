package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.BasicKpiRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.DistrictService;
import dev.thilanka.netrics.service.KpiDayService;
import dev.thilanka.netrics.service.StandardKpiService;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KpiDayServiceImpl implements KpiDayService {

    //-- Snapshot: Single whole KPI value considering the KPI and Period
    //-- Standard KPI: KPIs like 'E-RAB Setup Success Rate', 'DL Volume (Kbyte)'
    //-- Basic KPI: KPIs like 'Accessibility', 'Retainability'
    //-- Latest: Last day (newest) KPI
    //-- Compact: Contains only 'kpiLabel', 'value', 'difference with previous period', 'up/down with previous period'

    private final KpiDayRepository kpiDayRepository;
    private final StandardKpiService standardKpiService;
    private final BasicKpiRepository basicKpiRepository;
    private final RatService ratService;
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


    private KpiSnapshotCurrentPre getLatestKpiSnapshotWithPre(StandardKpi standardKpi, String period, Long ratId) {

        //-- GET KPI WITH LABEL, WORST-ORDER, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

        LocalDateTime timestamp = kpiDayRepository.getLatestDate(ratId);

        LocalDateTime preTimestamp = getLatestPreviousDate(period, ratId);
        if (standardKpi.getAggregation().equals("SUM")) {
            return kpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), ratId)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        } else {
            return kpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), ratId)
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        }
    }

    private KpiSnapshot getLatestKpiSnapshot(String kpiName, String period, Long ratId) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName,ratId);
        KpiSnapshotCurrentPre kpiData = getLatestKpiSnapshotWithPre(standardKpi, period, ratId);

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


    private KpiSnapshotDto getLatestKpiSnapshotWithDistrict(String kpiName, String period, String districtName, Long ratId) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName, ratId);
        LocalDateTime timestamp = getLatestDate(ratId);
        District district = districtService.findDistrictByName(districtName);

        return kpiDayRepository.findLatestKpiSnapshotByDistrict(
                        standardKpi.getId(),
                        timestamp,
                        getLatestPreviousDate(period, ratId),
                        getPeriod(period),
                        district.getId(),
                        ratId)
                .orElseThrow(() -> new RuntimeException("KPI Snapshot Query failed!"));
    }


    @Override
    @Cacheable(value = "basicKpiSnapshot", key = "#basicKpiName + '_' + #period + '_' + #ratName")
    public BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        BasicKpi basicKpi = basicKpiRepository.findByKpiNameAndRat(basicKpiName, rat)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));


        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (StandardKpi standardKpi : basicKpi.getStandardKpis()) {
            KpiSnapshot snapshot = getLatestKpiSnapshot(standardKpi.getKpiName(), period, rat.getId());
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

    @Override
    @Cacheable(value = "basicKpiSnapshot", key = "#basicKpiName + '_' + #period + '_' + #districtName + '_' + #ratName")
    public BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshotsWithDistrict(String basicKpiName, String period, String districtName, String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        BasicKpi basicKpi = basicKpiRepository.findByKpiNameAndRat(basicKpiName, rat)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));


        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (StandardKpi standardKpi : basicKpi.getStandardKpis()) {
            KpiSnapshotDto snapshotDto = getLatestKpiSnapshotWithDistrict(standardKpi.getKpiName(), period, districtName, rat.getId());

            kpiSnapshots.add(mapper.toKpiSnapshot(snapshotDto));
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
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpi(String kpiName, String period, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());
        LocalDateTime preTimestamp = getLatestPreviousDate(period, rat.getId());

        return kpiDayRepository.findWorstCells(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), rat.getId());
    }

    @Override
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + 'excludeZeroes' + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpiExcludeZeroes(String kpiName, String period, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());
        LocalDateTime preTimestamp = getLatestPreviousDate(period, rat.getId());

        return kpiDayRepository.findWorstCellsExcludeZeroes(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), rat.getId());
    }

    @Override
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #districtName + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpiAndDistrict(String kpiName, String period, String districtName, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName,ratName);
        District district = districtService.findDistrictByName(districtName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());
        LocalDateTime preTimestamp = getLatestPreviousDate(period, rat.getId());

        return kpiDayRepository.findWorstCellsByDistrict(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), district.getId(), rat.getId());

    }

    @Override
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #districtName + 'excludeZeroes' + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpiAndDistrictExcludeZeroes(String kpiName, String period, String districtName, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        District district = districtService.findDistrictByName(districtName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());
        LocalDateTime preTimestamp = getLatestPreviousDate(period, rat.getId());

        return kpiDayRepository.findWorstCellsByDistrictExcludeZeroes(standardKpi.getId(), timestamp, preTimestamp, getPeriod(period), district.getId(), rat.getId());

    }


    // ------------------------------ WORST-CELLS END ------------------------------------------------------------------


    // ------------------------------ CELL KPI - START -----------------------------------------------------------------

    @Override
    public List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName) {

        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());

        List<KpiData> kpiData = kpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, getPeriod(period), cellName, rat.getId());

        return kpiData.stream()
                .map(mapper::kpiDataToDto)
                .toList();
    }

    @Override
//    @Cacheable(value = "kpiData", key = "#kpiLabel + '_' + #period + '_' + #cellName")
    public List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period, String ratName) {

        StandardKpi standardKpi = standardKpiService.findByKpiLabel(kpiLabel, ratName);

        return getDataByKpiAndCell(standardKpi.getKpiName(), cellName, period, ratName);
    }


    // ------------------------------ CELL KPI - END -------------------------------------------------------------------

    // ------------------------------ KPI TREND - END -------------------------------------------------------------------

    @Override
    @Cacheable(value = "kpiTrend", key = "#standardKpiName + '_' + #period + '_' + #ratName")
    public List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());
        List<KpiTrend> kpiTrends = new ArrayList<>();

        if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
            kpiTrends = kpiDayRepository.findTrendDataSumByKpi(standardKpi.getId(), timestamp, getPeriod(period), rat.getId());
        } else
            kpiTrends = kpiDayRepository.findTrendDataAvgByKpi(standardKpi.getId(), timestamp, getPeriod(period), rat.getId());

        return kpiTrends.stream().map(mapper::kpiTrendToDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "kpiTrend", key = "#standardKpiName + '_' + #period + '_' + #districtName + '_' + #ratName")
    public List<KpiTrendDto> getTrendByKpiAndDistrict(String standardKpiName, String period, String districtName, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        District district = districtService.findDistrictByName(districtName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = getLatestDate(rat.getId());
        List<KpiTrend> kpiTrends = new ArrayList<>();

        if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
            kpiTrends = kpiDayRepository.findTrendDataSumByKpiAndDistrict(standardKpi.getId(), timestamp, getPeriod(period), district.getId(), rat.getId());
        } else
            kpiTrends = kpiDayRepository.findTrendDataAvgByKpiAndDistrict(standardKpi.getId(), timestamp, getPeriod(period), district.getId(), rat.getId());

        return kpiTrends.stream().map(mapper::kpiTrendToDto).collect(Collectors.toList());
    }


    // ------------------------------ KPI TREND - END -------------------------------------------------------------------


    @Override
    public List<KpiDataDto> findAll() {
        List<KpiDay> kpiDays = kpiDayRepository.findAll();

        return kpiDays
                .stream()
                .map(mapper::kpiDayToKpiDataDto)
                .toList();
    }

    @Override
    public KpiDataDto createLteFddKpiDay(KpiDay kpiDay) {
        KpiDay savedKpiDay = kpiDayRepository.save(kpiDay);
        return mapper.kpiDayToKpiDataDto(savedKpiDay);
    }

    private LocalDateTime getLatestDate(Long ratId) {
        return kpiDayRepository.getLatestDate(ratId);
    }

    private LocalDateTime getLatestPreviousDate(String period, Long ratId) {
        switch (period) {
            case "day" -> {
                return getLatestDate(ratId).minusDays(1);
            }
            case "week" -> {
                return getLatestDate(ratId).minusDays(7);
            }
            case "month" -> {
                return getLatestDate(ratId).minusDays(30);
            }
            case "quarter" -> {
                return getLatestDate(ratId).minusDays(90);
            }
            case "half-year" -> {
                return getLatestDate(ratId).minusDays(180);
            }
            case "year" -> {
                return getLatestDate(ratId).minusDays(365);
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
    public List<KpiDay> getKpiWithoutDistrict(String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        return kpiDayRepository.findKpiWithoutDistrict(rat.getId());
    }

}
