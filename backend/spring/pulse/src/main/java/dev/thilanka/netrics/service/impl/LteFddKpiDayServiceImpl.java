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

    @Override
    public List<LteFddKpiDaySnap> getAverage() {
        return lteFddKpiDayRepository.getKpiX();
    }

    @Override
    public LteFddKpiDaySnap[] getKpiSnapshot(String standardKpiName, String period) {
        LteFddStandardKpi standardKpi = lteFddStandardKpiService.findByKpiName(standardKpiName);

        LteFddKpiDaySnap[] kpiDaySnaps = new LteFddKpiDaySnap[2];   //-- To get Current value & Previous period value
        LocalDateTime timestamp = getLatestDate();  //-- Get latest date

        switch (period) {
            case "day" -> {
                kpiDaySnaps[0] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp, 0L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                kpiDaySnaps[1] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp.minusDays(1), 0L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case "week" -> {
                kpiDaySnaps[0] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp, 6L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                kpiDaySnaps[1] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp.minusDays(7), 6L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case "month" -> {
                kpiDaySnaps[0] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp, 29L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
                kpiDaySnaps[1] = lteFddKpiDayRepository.getKpiSnapshot(standardKpi.getId(), timestamp.minusDays(30), 29L)
                        .orElseThrow(() -> new RuntimeException("Cannot retrieve KPI values"));
            }
            case null, default -> kpiDaySnaps = null;
        }

        return kpiDaySnaps;
    }

    @Override
    public LteFddBasicKpiData getBasicKpiSnapshot(String basicKpiName, String period) {

        LteFddBasicKpiData basicKpiData = new LteFddBasicKpiData(); //-- To Store Basic KPI data and all component Standard KPI data
        LteFddKpiDaySnap[] basicKpiSnap = new LteFddKpiDaySnap[2];  //-- To Store Basic KPI's data

        basicKpiSnap[0] = new LteFddKpiDaySnap();
        basicKpiSnap[1] = new LteFddKpiDaySnap();

        //-- Get Basic KPI
        LteFddBasicKpi basicKpi = lteFddBasicKpiRepository.findByKpiName(basicKpiName)
                .orElseThrow(() -> new RuntimeException("Basic KPI not found by: " + basicKpiName));

        //-- Set Basic KPI ID into Entity's StandardKpi ID
        basicKpiSnap[0].setLteFddKpiId(basicKpi.getId());   //-- for current period
        basicKpiSnap[1].setLteFddKpiId(basicKpi.getId());   //-- for previous period

        for (LteFddStandardKpi standardKpi : basicKpi.getLteFddStandardKpis()) {

            LteFddKpiDaySnap[] snap = getKpiSnapshot(standardKpi.getKpiName(), period); //-- Get KPI Snapshot for each component Standard KPIs (both current and previous periods)

            basicKpiData.getStandardKpi().add(snap);    //-- Add queried Standard KPI snapshots to Basic KPI Data (list)

            basicKpiSnap[0].setKpiValueSum(basicKpiSnap[0].getKpiValueSum() + snap[0].getKpiValueSum());    //-- Add KPI Value (SUM) to current period
            basicKpiSnap[1].setKpiValueSum(basicKpiSnap[1].getKpiValueSum() + snap[1].getKpiValueSum());    //-- Add KPI Value (SUM) to previous period

            //-- Addition of Numerator values of component Standard KPI to Basic KPI's Numerator
            basicKpiSnap[0].setNumeratorKpiValueSum(basicKpiSnap[0].getNumeratorKpiValueSum() + snap[0].getNumeratorKpiValueSum());
            basicKpiSnap[1].setNumeratorKpiValueSum(basicKpiSnap[1].getNumeratorKpiValueSum() + snap[1].getNumeratorKpiValueSum());

            //-- Addition of Denominator values of component Standard KPI to Basic KPI's Denominator
            basicKpiSnap[0].setDenominatorKpiValueSum(basicKpiSnap[0].getDenominatorKpiValueSum() + snap[0].getDenominatorKpiValueSum());
            basicKpiSnap[1].setDenominatorKpiValueSum(basicKpiSnap[1].getDenominatorKpiValueSum() + snap[1].getDenominatorKpiValueSum());
        }

        basicKpiData.setBasicKpi(basicKpiSnap); //-- Set Basic KPI info into BasicKpiData Object

        return basicKpiData;
    }

    @Override
    public List<LteFddBasicKpiSnap> getBasicSnapshot(String basicKpiName, String period) {

        List<LteFddBasicKpiSnap> basicKpiSnaps = new ArrayList<>();

        LteFddBasicKpiSnap basicKpi = new LteFddBasicKpiSnap();

        LteFddBasicKpiData basicKpiData = getBasicKpiSnapshot(basicKpiName, period);

        if (basicKpiData.getBasicKpi()[0].getNumeratorKpiValueSum() == 0.0 && basicKpiData.getBasicKpi()[0].getDenominatorKpiValueSum() == 0.0) {

            LteFddBasicKpi basic = lteFddBasicKpiRepository
                    .findById(basicKpiData
                            .getBasicKpi()[0]
                            .getLteFddKpiId())
                    .orElseThrow(() -> new RuntimeException("Incorrect Basic KPI ID"));

            basicKpi.setBasic(true);
            basicKpi.setValue(basicKpiData.getBasicKpi()[0].getKpiValueSum());
            basicKpi.setDifference(basicKpiData.getBasicKpi()[0].getKpiValueSum() - basicKpiData.getBasicKpi()[1].getKpiValueSum());
            basicKpi.setUp((basicKpiData.getBasicKpi()[0].getKpiValueSum() - basicKpiData.getBasicKpi()[1].getKpiValueSum()) > 0);
            basicKpi.setKpiLabel(basic.getLabel());
            basicKpiSnaps.add(basicKpi);
        } else {
            LteFddBasicKpi basic = lteFddBasicKpiRepository
                    .findById(basicKpiData
                            .getBasicKpi()[0]
                            .getLteFddKpiId())
                    .orElseThrow(() -> new RuntimeException("Incorrect Basic KPI ID"));
            basicKpi.setBasic(true);
            basicKpi.setValue(basicKpiData.getBasicKpi()[0].getNumeratorKpiValueSum()/basicKpiData.getBasicKpi()[0].getDenominatorKpiValueSum());
            basicKpi.setDifference(basicKpiData.getBasicKpi()[0].getNumeratorKpiValueSum()/basicKpiData.getBasicKpi()[0].getDenominatorKpiValueSum() - basicKpiData.getBasicKpi()[1].getNumeratorKpiValueSum()/basicKpiData.getBasicKpi()[1].getDenominatorKpiValueSum());
            basicKpi.setUp((basicKpiData.getBasicKpi()[0].getNumeratorKpiValueSum()/basicKpiData.getBasicKpi()[0].getDenominatorKpiValueSum() - basicKpiData.getBasicKpi()[1].getNumeratorKpiValueSum()/basicKpiData.getBasicKpi()[1].getDenominatorKpiValueSum()) > 0);
            basicKpi.setKpiLabel(basic.getLabel());
            basicKpiSnaps.add(basicKpi);
        }

        for (LteFddKpiDaySnap[] snap: basicKpiData.getStandardKpi()){
            LteFddBasicKpiSnap standardKpi = new LteFddBasicKpiSnap();

            LteFddStandardKpi kpi = lteFddStandardKpiRepository
                    .findById(snap[0].getLteFddKpiId())
                    .orElseThrow(()-> new RuntimeException("Standard KPI ID incorrect"));

            standardKpi.setKpiLabel(kpi.getLabel());
            standardKpi.setBasic(false);

            if (snap[0].getNumeratorKpiValueSum() == 0.0 && snap[0].getDenominatorKpiValueSum() == 0){
                standardKpi.setValue(snap[0].getKpiValueSum());
                standardKpi.setDifference(snap[0].getKpiValueSum() - snap[1].getKpiValueSum());
                standardKpi.setUp((snap[0].getKpiValueSum() - snap[1].getKpiValueSum()) > 0);
            } else {
                standardKpi.setValue(snap[0].getNumeratorKpiValueSum()/snap[0].getDenominatorKpiValueSum());
                standardKpi.setDifference(snap[0].getNumeratorKpiValueSum()/snap[0].getDenominatorKpiValueSum() - snap[1].getNumeratorKpiValueSum()/snap[1].getDenominatorKpiValueSum());
                standardKpi.setUp((snap[0].getNumeratorKpiValueSum()/snap[0].getDenominatorKpiValueSum() - snap[1].getNumeratorKpiValueSum()/snap[1].getDenominatorKpiValueSum()) > 0);
            }

            basicKpiSnaps.add(standardKpi);

        }




        return basicKpiSnaps;
    }

    private LocalDateTime getLatestDate() {
        LocalDateTime latestDate = lteFddKpiDayRepository.getLatestDate();
        System.out.println(latestDate);
        return latestDate;
    }

}
