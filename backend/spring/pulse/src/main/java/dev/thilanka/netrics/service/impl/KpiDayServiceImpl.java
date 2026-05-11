package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.DataQueryException;
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
    private final GranularityService granularityService;
    private final AreaService areaService;
    private final BandService bandService;
    private final KpiHourService kpiHourService;

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


    private KpiSnapshotCurrentPre getLatestKpiSnapshotWithPre(StandardKpi standardKpi, String period, Rat rat, Granularity granularity) {

        //-- GET KPI WITH LABEL, WORST-ORDER, VALUE, PRE-VALUE, CALCULATED VALUE, CALCULATED PRE-VALUE

        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity);
        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat, granularity);

        if (standardKpi.getAggregation().equals("SUM")) {
            return kpiDayRepository.findLatestSumKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, dateService.getPeriod(period), rat.getId(), granularity.getId())
                    .orElseThrow(() -> new DataQueryException("Cannot retrieve KPI values"));
        } else {
            return kpiDayRepository.findLatestAvgKpiSnapshotWithPre(standardKpi.getId(), timestamp, preTimestamp, dateService.getPeriod(period), rat.getId(), granularity.getId())
                    .orElseThrow(() -> new DataQueryException("Cannot retrieve KPI values"));
        }
    }

    private KpiSnapshot getLatestKpiSnapshot(String kpiName, String period, Rat rat, Granularity granularity) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName, rat.getId());
        KpiSnapshotCurrentPre kpiData = getLatestKpiSnapshotWithPre(standardKpi, period, rat, granularity);

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


    private KpiSnapshotDto getLatestKpiSnapshotWithDistrict(String kpiName, String period, String districtName, Rat rat, Granularity granularity) {

        //-- GET KPI WITH LABEL, IS-BASIC, VALUE, PREVIOUS VALUE, DIFFERENCE, IMPROVED

        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName, rat.getId());
        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity);
        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat, granularity)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());
        District district = districtService.findDistrictByName(districtName);

        return kpiDayRepository.findLatestKpiSnapshotByDistrict(
                        standardKpi.getId(),
                        timestamp,
                        preTimestamp,
                        dateService.getPeriod(period),
                        district.getId(),
                        rat.getId(),
                        granularity.getId())
                .orElseThrow(() -> new DataQueryException("KPI Snapshot Query failed"));
    }

    private KpiSnapshotDto getLatestKpiSnapshotByArea(String kpiName, LocalDateTime currStart, LocalDateTime currEnd, LocalDateTime preStart, LocalDateTime preEnd, Area area, Rat rat, Granularity granularity) {
        StandardKpi standardKpi = standardKpiService.findByKpiNameAndRatId(kpiName, rat.getId());

        return kpiDayRepository.findKpiSnapshotByArea(standardKpi.getId(), currStart, currEnd, preStart, preEnd, area.getId(), rat.getId(), granularity.getId())
                .orElseThrow(() -> new DataQueryException("KPI Snapshot Query failed"));
    }


    @Override
    @Cacheable(value = "basicKpiSnapshot", key = "#basicKpiName + '_' + #period + '_' + #granularityName + '_' + #ratName")
    public BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshots(String basicKpiName, String period, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        BasicKpiWithStandardKpiDto basicKpi = basicKpiService.getByKpiNameAndRat(basicKpiName, ratName);    //-- To accommodate Async execution of warm-up without having to Lazy load StandardKpis from BasicKpi

        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (StandardKpiDto standardKpi : basicKpi.standardKpis()) {
            KpiSnapshot snapshot = getLatestKpiSnapshot(standardKpi.kpiName(), period, rat, granularity);
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
    @Cacheable(value = "basicKpiSnapshot", key = "#basicKpiName + '_' + #period + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public BasicKpiSnapshot getLatestBasicAndStandardKpiSnapshotsByArea(String basicKpiName, String period, String areaName, String ratName, String granularityName) {

        // DO NOT USE THIS METHOD FOR ASYNC WARM-UP CACHES. LAZY LOADING WILL THROW EXCEPTION

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        Area area = areaService.findAreaByName(areaName);

        LocalDateTime currEnd = dateService.getLatestDate(rat, granularity).toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
        LocalDateTime currStart = dateService.getStartDate(currEnd, period).toLocalDate().atStartOfDay();

        LocalDateTime prevEnd = dateService.getPreviousDate(currEnd, period);
        LocalDateTime prevStart = dateService.getStartDate(prevEnd, period).toLocalDate().atStartOfDay();

        BasicKpi basicKpi = basicKpiService.findByKpiName(basicKpiName, rat);

        BasicKpiSnapshot basicKpiSnapshot = new BasicKpiSnapshot();

        List<KpiSnapshot> kpiSnapshots = new ArrayList<>();

        for (StandardKpi standardKpi : basicKpi.getStandardKpis()) {
            KpiSnapshotDto snapshotDto = getLatestKpiSnapshotByArea(standardKpi.getKpiName(), currStart, currEnd, prevStart, prevEnd, area, rat, granularity);

            kpiSnapshots.add(mapper.toKpiSnapshot(snapshotDto));    //TODO: USE KpiSnapshot class (It has Boolean for improved)
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
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #excludeZeroes + '_' + #granularityName + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpi(String kpiName, String period, boolean excludeZeroes, String ratName, String granularityName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity);
        LocalDateTime currentStart = timestamp.minusDays(dateService.getPeriod(period)).toLocalDate().atStartOfDay();

        LocalDateTime preTimestamp = dateService.getLatestPreviousDate(period, rat, granularity)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());
        LocalDateTime previousStart = preTimestamp.minusDays(dateService.getPeriod(period)).toLocalDate().atStartOfDay();

        return kpiDayRepository.findWorstCells(standardKpi.getId(), timestamp, currentStart, preTimestamp, previousStart, rat.getId(), excludeZeroes, granularity.getId());
    }

    @Override
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #excludeZeroes + '_' + #limit + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpiAndArea(String kpiName, String period, boolean excludeZeroes, int limit, String areaName, String ratName, String granularityName) {

        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, rat);
        Area area = areaService.findAreaByName(areaName);

        LocalDateTime currEnd = dateService.getLatestDate(rat, granularity).toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
        LocalDateTime currStart = dateService.getStartDate(currEnd, period).toLocalDate().atStartOfDay();

        LocalDateTime prevEnd = dateService.getPreviousDate(currEnd, period);
        LocalDateTime prevStart = dateService.getStartDate(prevEnd, period).toLocalDate().atStartOfDay();

        return kpiDayRepository.findWorstCellsByArea(standardKpi.getId(), currStart, currEnd, prevStart, prevEnd, limit, area.getId(), rat.getId(), excludeZeroes, granularity.getId());
    }

    @Override
    @Cacheable(value = "worstCells", key = "#kpiName + '_' + #period + '_' + #excludeZeroes + '_' + #limit + '_' + #areaName + '_' + #bandName + '_' + #granularityName + '_' + #ratName")
    public List<WorstCellsDto> getWorstCellsByKpiAreaAndBand(String kpiName, String period, boolean excludeZeroes, int limit, String ratName, String areaName, String granularityName, String bandName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(kpiName, rat);
        Area area = areaService.findAreaByName(areaName);
        Band band = bandService.findByName(bandName);

        LocalDateTime currEnd = dateService.getLatestDate(rat, granularity).toLocalDate().atStartOfDay().plusSeconds(granularity.getPlusSeconds());
        LocalDateTime currStart = dateService.getStartDate(currEnd, period).toLocalDate().atStartOfDay();

        LocalDateTime prevEnd = dateService.getPreviousDate(currEnd, period);
        LocalDateTime prevStart = dateService.getStartDate(prevEnd, period).toLocalDate().atStartOfDay();

        return kpiDayRepository.findWorstCellsByAreaAndBand(standardKpi.getId(), currStart, currEnd, prevStart, prevEnd, limit, area.getId(), rat.getId(), excludeZeroes, granularity.getId(), band.getId());
    }

    @Override
    public List<CellNameDto> getCellNamesByTimestamps(LocalDateTime timestamp, LocalDateTime preTimestamp, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        return kpiDayRepository.getCellNamesByTimestamps(timestamp, preTimestamp, rat.getId(), granularity.getId());
    }

    @Override
    public List<CellDto> getCellsByTimestamp(LocalDateTime timestamp, LocalDateTime preTimestamp, Rat rat, Granularity granularity) {
        return kpiDayRepository.getCellsByTimestamp(timestamp, preTimestamp, rat.getId(), granularity.getId());
    }


    // ------------------------------ WORST-CELLS END ------------------------------------------------------------------


    // ------------------------------ CELL KPI - START -----------------------------------------------------------------

    @Override
    @Cacheable(value = "cellKpiTrend", key = "#standardKpiName +'_' + #cellName + '_' + #period + '_' + #granularityName + '_' + #ratName")
    public List<KpiDataDto> getDataByKpiAndCell(String standardKpiName, String cellName, String period, String ratName, String granularityName) {

        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());

        LocalDateTime startTimestamp = dateService.getPreviousDate(timestamp, period).toLocalDate().atStartOfDay();

//        Long periodValue = dateService.getPeriod(period);


        if (granularity.getName().equals("hour")) {        //-- Query trend data hourly
            return kpiHourService.getDataByKpiAndCell(standardKpi.getId(), cellName, timestamp, startTimestamp, rat.getId(), granularity.getId());
        } else {
            List<KpiData> kpiData = kpiDayRepository.findDataByKpiAndCell(standardKpi.getId(), timestamp, startTimestamp, cellName, rat.getId(), granularity.getId());
            return kpiData.stream()
                    .map(mapper::kpiDataToDto)
                    .collect(Collectors.toList());
        }
    }


    @Override
    @Cacheable(value = "cellKpiTrend", key = "#kpiLabel +'_' + #cellName + '_' + #period + '_' + #granularityName + '_' + #ratName")
    public List<KpiDataDto> getDataByKpiLabelAndCell(String kpiLabel, String cellName, String period, String ratName, String granularityName) {

        StandardKpi standardKpi = standardKpiService.findByKpiLabel(kpiLabel, ratName);

        return getDataByKpiAndCell(standardKpi.getKpiName(), cellName, period, ratName, granularityName);
    }

    // ------------------------------ CELL KPI - END -------------------------------------------------------------------

    // ------------------------------ KPI TREND - END -------------------------------------------------------------------

    @Override
    @Cacheable(value = "kpiTrend", key = "#standardKpiName + '_' + #period + '_' + #granularityName + '_' + #ratName")
    public List<KpiTrendDto> getTrendByKpi(String standardKpiName, String period, String ratName, String granularityName) {
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, ratName);
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);

        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());
        LocalDateTime startTimestamp = dateService.getPreviousDate(timestamp, period)
                .toLocalDate().atStartOfDay();

        List<KpiTrend> kpiTrends = kpiDayRepository.findTrendDataByKpi(standardKpi.getId(), timestamp, startTimestamp, rat.getId(), granularity.getId());

        return kpiTrends.stream().map(mapper::kpiTrendToDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "kpiTrend", key = "#standardKpiName + '_' + #period + '_' + #areaName + '_' + #granularityName + '_' + #ratName")
    public List<KpiTrendDto> getTrendByKpiAndArea(String standardKpiName, String period, String areaName, String ratName, String granularityName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, rat);
        Area area = areaService.findAreaByName(areaName);

        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());

        LocalDateTime startTimestamp = dateService.getPreviousDate(timestamp, period)
                .toLocalDate().atStartOfDay();
        List<KpiTrend> kpiTrends = kpiDayRepository.findTrendDataByKpiAndArea(standardKpi.getId(), timestamp, startTimestamp, area.getId(), rat.getId(), granularity.getId());
        return kpiTrends.stream().map(mapper::kpiTrendToDto).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "kpiTrend", key = "#standardKpiName + '_' + #period + '_' + #areaName + '_' + #bandName + '_' + #granularityName + '_' + #ratName")
    public List<KpiTrendDto> getTrendByKpiAreaAndBand(String standardKpiName, String period, String areaName, String ratName, String granularityName, String bandName) {
        Rat rat = ratService.findRatByName(ratName);
        Granularity granularity = granularityService.findGranularityByName(granularityName);
        StandardKpi standardKpi = standardKpiService.findByKpiName(standardKpiName, rat);
        Area area = areaService.findAreaByName(areaName);
        Band band = bandService.findByName(bandName);

        LocalDateTime timestamp = dateService.getLatestDate(rat, granularity)
                .toLocalDate()
                .atStartOfDay()
                .plusSeconds(granularity.getPlusSeconds());

        LocalDateTime startTimestamp = dateService.getPreviousDate(timestamp, period)
                .toLocalDate().atStartOfDay();
        List<KpiTrend> kpiTrends = kpiDayRepository.findTrendDataByKpiAreaAndBand(standardKpi.getId(), timestamp, startTimestamp, area.getId(), rat.getId(), granularity.getId(), band.getId());
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
