package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.*;
import dev.thilanka.netrics.entity.*;
import dev.thilanka.netrics.entity.district.District;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.BasicKpiRepository;
import dev.thilanka.netrics.repository.KpiDayRepository;
import dev.thilanka.netrics.service.*;
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
    //-- Keep Cache key as "_ratName" because cacheEvict based on RAT depends on it

    private final KpiDayRepository kpiDayRepository;
    private final StandardKpiService standardKpiService;
    private final BasicKpiRepository basicKpiRepository;
    private final BasicKpiService basicKpiService;
    private final RatService ratService;
    private final DistrictService districtService;
    private final Mapper mapper;
    private final DateService dateService;

    @Override
    public boolean checkImproved(String worstOrder, Double difference) {
        if (Objects.equals(worstOrder, "ASC")) {
            return difference > 0;
        } else if (Objects.equals(worstOrder, "DESC")) {
            return difference < 0;
        }
        return false;
    }

    //------------------------------- KPI-SNAPSHOT START ---------------------------------------------------------------


    private KpiSnapshotCurrentPre getLatestKpiSnapshotWithPre(StandardKpi standardKpi, String period, Rat rat) {

        //-- GET KPI WITH LABEL, WORST-ORDER, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

        LocalDateTime timestamp = dateService.getLatestDate(rat);
        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);

        if (standardKpi.getAggregation().equals("SUM")) {
            return kpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, dateService.getPeriod(period), rat.getId())
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        } else {
            return kpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, dateService.getPeriod(period), rat.getId())
                    .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
        }
    }

    private KpiSnapshot getLatestKpiSnapshot(String kpiName, String period, Rat rat) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName, rat.getId());
        KpiSnapshotCurrentPre kpiData = getLatestKpiSnapshotWithPre(standardKpi, period, rat);

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


    private KpiSnapshotDto getLatestKpiSnapshotWithDistrict(String kpiName, String period, String districtName, Rat rat) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName, rat.getId());
        LocalDateTime timestamp = dateService.getLatestDate(rat);
        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);
        District district = districtService.findDistrictByName(districtName);

        return kpiDayRepository.findLatestKpiSnapshotByDistrict(
                        standardKpi.getId(),
                        timestamp,
                        preTimestamp,
                        dateService.getPeriod(period),
                        district.getId(),
                        rat.getId())
                .orElseThrow(() -> new RuntimeException("KPI Snapshot Query failed!"));
    }


    @Override
    @Cacheable(value = "basicKpiSnapshot", key = "#basicKpiName + '_' + #period + '_' + #ratName")
    public BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period, String ratName) {
        Rat rat = ratService.findRatByName(ratName);

        BasicKpiWithStandardKpiDto basicKpi = basicKpiService.findByKpiNameAndRat(basicKpiName, ratName);    //-- To accommodate Async execution of warm-up without having to Lazy load StandardKpis from BasicKpi

        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (StandardKpiDto standardKpi : basicKpi.standardKpis()) {
            KpiSnapshot snapshot = getLatestKpiSnapshot(standardKpi.kpiName(), period, rat);
            kpiSnapshots.add(snapshot);
        }
        basicKpiSnapshot.setStandardKpis(kpiSnapshots);

        //-- CREATE BASIC-KPI'S DATA
        basicKpiSnapshot.setKpiLabel(basicKpi.label());
        basicKpiSnapshot.setUnit(basicKpi.unit());

        basicKpiSnapshot.setPreviousValue(1.0);

        if (Objects.equals(basicKpi.aggregation(), "MULTIPLY")) {

            basicKpiSnapshot.setValue(1.0);

            //-- Calculate Value & Pre-Value by multiplying component Standard-KPI values. [IMPORTANT: Assume component Standard-KPI are percentages]
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * snapshot.getValue() / 100.0);
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * snapshot.getPreviousValue() / 100.0);
            }
            basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * 100.0); //-- To avoid presenting decimals as percentages
            basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * 100.0); //-- To avoid presenting decimals as percentages

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.worstOrder(), basicKpiSnapshot.getDifference()));

        } else if (Objects.equals(basicKpi.aggregation(), "SUM")) {

            basicKpiSnapshot.setValue(0.0);

            //-- Calculate Value & Pre-Value by adding component Standard-KPI values.
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() + snapshot.getValue());
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() + snapshot.getPreviousValue());
            }

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.worstOrder(), basicKpiSnapshot.getDifference()));

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
        BasicKpiWithStandardKpiDto basicKpi = basicKpiService.findByKpiNameAndRat(basicKpiName, ratName);    //-- To accommodate Async execution of warm-up without having to Lazy load StandardKpis from BasicKpi


        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (StandardKpiDto standardKpi : basicKpi.standardKpis()) {
            KpiSnapshotDto snapshotDto = getLatestKpiSnapshotWithDistrict(standardKpi.kpiName(), period, districtName, rat);

            kpiSnapshots.add(mapper.toKpiSnapshot(snapshotDto));
        }
        basicKpiSnapshot.setStandardKpis(kpiSnapshots);

        //-- CREATE BASIC-KPI'S DATA
        basicKpiSnapshot.setKpiLabel(basicKpi.label());
        basicKpiSnapshot.setUnit(basicKpi.unit());

        basicKpiSnapshot.setPreviousValue(1.0);

        if (Objects.equals(basicKpi.aggregation(), "MULTIPLY")) {

            basicKpiSnapshot.setValue(1.0);

            //-- Calculate Value & Pre-Value by multiplying component Standard-KPI values. [IMPORTANT: Assume component Standard-KPI are percentages]
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * snapshot.getValue() / 100.0);
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * snapshot.getPreviousValue() / 100.0);
            }
            basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() * 100.0); //-- To avoid presenting decimals as percentages
            basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() * 100.0); //-- To avoid presenting decimals as percentages

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.worstOrder(), basicKpiSnapshot.getDifference()));

        } else if (Objects.equals(basicKpi.aggregation(), "SUM")) {

            basicKpiSnapshot.setValue(0.0);

            //-- Calculate Value & Pre-Value by adding component Standard-KPI values.
            for (KpiSnapshot snapshot : kpiSnapshots) {
                basicKpiSnapshot.setValue(basicKpiSnapshot.getValue() + snapshot.getValue());
                basicKpiSnapshot.setPreviousValue(basicKpiSnapshot.getPreviousValue() + snapshot.getPreviousValue());
            }

            basicKpiSnapshot.setDifference(basicKpiSnapshot.getValue() - basicKpiSnapshot.getPreviousValue());
            basicKpiSnapshot.setImproved(checkImproved(basicKpi.worstOrder(), basicKpiSnapshot.getDifference()));

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
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #excludeZeroes + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpi(String kpiName, String period, boolean excludeZeroes, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);

        LocalDateTime timestamp = dateService.getLatestDate(rat);
        LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);
        LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period));

        return kpiDayRepository.findWorstCells(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, rat.getId(), excludeZeroes);
    }

    @Override
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #districtName + '_' + #excludeZeroes + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpiAndDistrict(String kpiName, String period, boolean excludeZeroes, String districtName, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        District district = districtService.findDistrictByName(districtName);
        Rat rat = ratService.findRatByName(ratName);

        LocalDateTime timestamp = dateService.getLatestDate(rat);
        LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period));

        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat);
        LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period));

        return kpiDayRepository.findWorstCellsByDistrict(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, district.getId(), rat.getId(), excludeZeroes);
    }


    // ------------------------------ WORST-CELLS END ------------------------------------------------------------------


    // ------------------------------ CELL KPI - START -----------------------------------------------------------------

    @Override
    @Cacheable(value = "cellKpiTrend", key = "#standardKpiName +'_' + #cellName + '_' + #period + '_' + #ratName")
    public List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName) {

        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = dateService.getLatestDate(rat);
        Long periodValue = dateService.getPeriod(period);

        List<KpiData> kpiData = kpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, periodValue, cellName, rat.getId());

        return kpiData.stream()
                .map(mapper::kpiDataToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "cellKpiTrend", key = "#kpiLabel +'_' + #cellName + '_' + #period + '_' + #ratName")
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
        LocalDateTime timestamp = dateService.getLatestDate(rat);
        Long periodValue = dateService.getPeriod(period);
        List<KpiTrend> kpiTrends = new ArrayList<>();

        if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
            kpiTrends = kpiDayRepository.findTrendDataSumByKpi(standardKpi.getId(), timestamp, periodValue, rat.getId());
        } else
            kpiTrends = kpiDayRepository.findTrendDataAvgByKpi(standardKpi.getId(), timestamp, periodValue, rat.getId());

        return kpiTrends.stream().map(mapper::kpiTrendToDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "kpiTrend", key = "#standardKpiName + '_' + #period + '_' + #districtName + '_' + #ratName")
    public List<KpiTrendDto> getTrendByKpiAndDistrict(String standardKpiName, String period, String districtName, String ratName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        District district = districtService.findDistrictByName(districtName);
        Rat rat = ratService.findRatByName(ratName);
        LocalDateTime timestamp = dateService.getLatestDate(rat);
        Long periodValue = dateService.getPeriod(period);
        List<KpiTrend> kpiTrends = new ArrayList<>();

        if (Objects.equals(standardKpi.getAggregation(), "SUM")) {
            kpiTrends = kpiDayRepository.findTrendDataSumByKpiAndDistrict(standardKpi.getId(), timestamp, periodValue, district.getId(), rat.getId());
        } else
            kpiTrends = kpiDayRepository.findTrendDataAvgByKpiAndDistrict(standardKpi.getId(), timestamp, periodValue, district.getId(), rat.getId());

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

    @Override
    public List<KpiDay> getKpiWithoutDistrict(String ratName) {
        Rat rat = ratService.findRatByName(ratName);
        return kpiDayRepository.findKpiWithoutDistrict(rat.getId());
    }

}
