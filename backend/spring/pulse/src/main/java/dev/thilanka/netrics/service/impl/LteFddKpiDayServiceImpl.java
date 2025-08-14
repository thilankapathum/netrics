package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.KpiDataDto;
import dev.thilanka.netrics.dto.KpiDataFractionDto;
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


    private KpiSnapshot[] getLatestKpiSnapshot(String standardKpiName, String period) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);

        KpiSnapshot[] kpiSnapshots = new KpiSnapshot[2];   //-- To get Current values & Previous period values. [0] holds current values. [1] holds previous values
        LocalDateTime timestamp = getLatestDate();  //-- Get latest date

        switch (period) {
            case "day" -> {
                kpiSnapshots[0] = lteFddKpiDayRepository.findLatestKpiSnapshot(standardKpi.getId(), timestamp, 0L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                kpiSnapshots[1] = lteFddKpiDayRepository.findLatestKpiSnapshot(standardKpi.getId(), timestamp.minusDays(1), 0L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case "week" -> {
                kpiSnapshots[0] = lteFddKpiDayRepository.findLatestKpiSnapshot(standardKpi.getId(), timestamp, 6L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                kpiSnapshots[1] = lteFddKpiDayRepository.findLatestKpiSnapshot(standardKpi.getId(), timestamp.minusDays(7), 6L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case "month" -> {
                kpiSnapshots[0] = lteFddKpiDayRepository.findLatestKpiSnapshot(standardKpi.getId(), timestamp, 29L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                kpiSnapshots[1] = lteFddKpiDayRepository.findLatestKpiSnapshot(standardKpi.getId(), timestamp.minusDays(30), 29L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case null, default -> kpiSnapshots = null;
        }

        return kpiSnapshots;
    }

    private BasicStandardKpiData getBasicStandardKpiSnapshot(String basicKpiName, String period) {

        BasicStandardKpiData basicStandardKpiData = new BasicStandardKpiData(); //-- To Store Basic KPI data and all component Standard KPI data
        KpiSnapshot[] basicKpiSnapshot = new KpiSnapshot[2];  //-- To Store Basic KPI's data

        basicKpiSnapshot[0] = new KpiSnapshot();
        basicKpiSnapshot[1] = new KpiSnapshot();

        //-- Get Basic KPI
        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(basicKpiName)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));

        //-- Set Basic KPI ID into Entity's StandardKpi ID
        basicKpiSnapshot[0].setLabel(basicKpi.getLabel());   //-- for current period
        basicKpiSnapshot[1].setLabel(basicKpi.getLabel());   //-- for previous period

        //-- Get all Standard KPI of Basic KPI
        for (LteFddStandardKpi standardKpi : basicKpi.getLteFddStandardKpis()) {

            KpiSnapshot[] snapshot = getLatestKpiSnapshot(standardKpi.getKpiName(), period); //-- Get KPI Snapshot for each component Standard KPIs (both current and previous periods)

            basicStandardKpiData.getStandardKpi().add(snapshot);    //-- Add queried Standard KPI snapshots to Basic+Standard KPI Data (list)

            //-- Add (Sum) KpiValue of each component KPI to Basic KPI's KpiValue
            basicKpiSnapshot[0].setKpiValueSum(getDoubleNonNull(basicKpiSnapshot[0].getKpiValueSum()) + getDoubleNonNull(snapshot[0].getKpiValueSum()));
            basicKpiSnapshot[1].setKpiValueSum(getDoubleNonNull(basicKpiSnapshot[1].getKpiValueSum()) + getDoubleNonNull(snapshot[1].getKpiValueSum()));//-- Add KPI Value (SUM) to previous period

            //-- Addition of Numerator values of component Standard KPI to Basic KPI's Numerator
            basicKpiSnapshot[0].setNumeratorKpiValueSum(getDoubleNonNull(basicKpiSnapshot[0].getNumeratorKpiValueSum())+ getDoubleNonNull(snapshot[0].getNumeratorKpiValueSum()));
            basicKpiSnapshot[1].setNumeratorKpiValueSum(getDoubleNonNull(basicKpiSnapshot[1].getNumeratorKpiValueSum())+ getDoubleNonNull(snapshot[1].getNumeratorKpiValueSum()));

            //-- Addition of Denominator values of component Standard KPI to Basic KPI's Denominator
            basicKpiSnapshot[0].setDenominatorKpiValueSum(getDoubleNonNull(basicKpiSnapshot[0].getDenominatorKpiValueSum()) + getDoubleNonNull(snapshot[0].getDenominatorKpiValueSum()));
            basicKpiSnapshot[1].setDenominatorKpiValueSum(getDoubleNonNull(basicKpiSnapshot[1].getDenominatorKpiValueSum()) + getDoubleNonNull(snapshot[1].getDenominatorKpiValueSum()));
        }

        basicStandardKpiData.setBasicKpi(basicKpiSnapshot); //-- Set Basic KPI info into BasicKpiData Object

        return basicStandardKpiData;
    }

    @Override
    public List<CompactKpiSnapshot> getCompactBasicStandardKpiSnapshot(String basicKpiName, String period) {

        List<CompactKpiSnapshot> kpiSnapshots = new ArrayList<>();

        BasicStandardKpiData basicStandardKpiData = getBasicStandardKpiSnapshot(basicKpiName, period);

        //-- Set Compact KPI values for Basic KPI.
        CompactKpiSnapshot basicKpi = makeCompactKpiSnapshot(basicStandardKpiData.getBasicKpi(), true);
        basicKpi.setKpiLabel(basicStandardKpiData.getBasicKpi()[0].getLabel());
        kpiSnapshots.add(basicKpi);

        //-- Set Compact KPI values for Basic KPI's component Standard KPIs.
        for (KpiSnapshot[] snapshots : basicStandardKpiData.getStandardKpi()) {
            CompactKpiSnapshot standardKpi = makeCompactKpiSnapshot(snapshots, false);

            kpiSnapshots.add(standardKpi);
        }
        return kpiSnapshots;
    }

    private static CompactKpiSnapshot makeCompactKpiSnapshot(KpiSnapshot[] snapshot, boolean basic) {
        CompactKpiSnapshot standardKpi = new CompactKpiSnapshot();

        if (!snapshot[0].getLabel().isBlank()) {
            standardKpi.setKpiLabel(snapshot[0].getLabel());
        }
        standardKpi.setBasic(basic);

        //-- Check for condition: Numerator and Denominator values are 0.0 and KpiValue is not 0.0
        if (snapshot[0].getNumeratorKpiValueSum() == 0.0 && snapshot[0].getDenominatorKpiValueSum() == 0 && snapshot[0].getKpiValueSum() != 0.0) {
            standardKpi.setValue(snapshot[0].getKpiValueSum());
            standardKpi.setDifference(snapshot[0].getKpiValueSum() - snapshot[1].getKpiValueSum());
            standardKpi.setUp((snapshot[0].getKpiValueSum() - snapshot[1].getKpiValueSum()) > 0);
        } else {
            standardKpi.setValue(snapshot[0].getNumeratorKpiValueSum() / snapshot[0].getDenominatorKpiValueSum());
            standardKpi.setDifference(calculateDifferenceOfFractions(snapshot));
            standardKpi.setUp(checkDifferenceUp(snapshot));
        }
        return standardKpi;
    }

    private static Double calculateDifferenceOfFractions(KpiSnapshot[] snapshot) {
        return (getDoubleNonNull(snapshot[0].getNumeratorKpiValueSum()) / getDoubleNonNull(snapshot[0].getDenominatorKpiValueSum()))
                - (getDoubleNonNull(snapshot[1].getNumeratorKpiValueSum()) / getDoubleNonNull(snapshot[1].getDenominatorKpiValueSum()));
    }

    private static boolean checkDifferenceUp(KpiSnapshot[] snapshot) {
        return ((getDoubleNonNull(snapshot[0].getNumeratorKpiValueSum()) / getDoubleNonNull(snapshot[0].getDenominatorKpiValueSum()))
                - (getDoubleNonNull(snapshot[1].getNumeratorKpiValueSum()) / getDoubleNonNull(snapshot[1].getDenominatorKpiValueSum()))) > 0;
    }

    private LocalDateTime getLatestDate() {
        LocalDateTime latestDate = lteFddKpiDayRepository.getLatestDate();
        System.out.println(latestDate);
        return latestDate;
    }

    private static Double getDoubleNonNull(Double value){
        if (value == null) {
            return 0.0;
        }else return value;

    }

}
