package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CacheWarmupImpl implements CacheWarmup {
    private final KpiDayService kpiDayService;
    private final BasicKpiService basicKpiService;
    private final DistrictService districtService;
    private final StandardKpiService standardKpiService;
    private final String[] periods = {"day"};


    @Override
    public void warmupCache() {
        warmupLteFddBasicKpiSnapshotCache();
        warmupLteFddKpiTrendCache();
        warmupLteFddWorstCellCache();
        System.out.println("Cache warmup complete!");
    }

    private void warmupLteFddBasicKpiSnapshotCache() {
        List<BasicKpiDto> basicKpis = basicKpiService.getAllByRat("ltefdd"); //TODO
        List<DistrictDto> districts = districtService.getAll();
        for (String period : periods) {
            for (BasicKpiDto dto : basicKpis) {
                try {
                    //TODO: Implement better RAT name
                    kpiDayService.getLatestBasicAndStandardKpiSnapshots(dto.kpiName(), period, "ltefdd");
                    System.out.println("Cache BasicKpi warmed-up: " + dto.kpiName() + "-" + period);
                } catch (Exception e) {
                    System.out.println("Error warming cache for: " + dto.kpiName() + "-" + period);
//                    e.printStackTrace();
                }
                for (DistrictDto district : districts) {
                    try {
                        // TODO: Implement better RAT name
                        kpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(dto.kpiName(), period, district.name(), "ltefdd");
                        System.out.println("Cache BasicKpi warmed-up: " + dto.kpiName() +"-"+ period +"-"+ district.name());
                    } catch (Exception e) {
                        System.out.println("Error warming cache for: " + dto.kpiName() +"-" + period +"-" + district.name());
//                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void warmupLteFddKpiTrendCache() {
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat("ltefdd"); //TODO
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                //TODO: Implement Better RAT name
                kpiDayService.getTrendByKpi(standardKpi.kpiName(), "month", "ltefdd");
                System.out.println("Cache LteFddKpiTrend warmed-up (month): " + standardKpi.kpiName());
            } catch (Exception e) {
                System.out.println("Error while warming cache for (month): " + standardKpi.kpiName());
            }
            for (DistrictDto district : districts) {
                try {
                    //TODO: Implement better RAT name
                    kpiDayService.getTrendByKpiAndDistrict(standardKpi.kpiName(), "month", district.name(), "ltefdd");
                    System.out.println("Cache LteFddKpiTrend warmed-up (month): " + standardKpi.kpiName() +"-" + district.name());
                } catch (Exception e) {
                    System.out.println("Error while warming cache for (month): " + standardKpi.kpiName()+ "-" + district.name());
                }
            }
        }
    }

    private void warmupLteFddWorstCellCache(){
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat("ltefdd"); //TODO
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi: lteFddStandardKpis){
            try{
                //TODO: Implement better RAT name
                kpiDayService.getWorstCellsByKpi(standardKpi.kpiName(),"day", "ltefdd");
                System.out.println("Cache LteFddWorstCells warmed-up: " + standardKpi.kpiName());
            } catch (Exception e){
                System.out.println("Error while warming cache for: " + standardKpi.kpiName());
            }

            for (DistrictDto district: districts){
                try {
                    //TODO: Implement better RAT name
                    kpiDayService.getWorstCellsByKpiAndDistrict(standardKpi.kpiName(), "day", district.name(), "ltefdd");
                    System.out.println("Cache LteFddWorstCell warmed-up: " + standardKpi.kpiName() + "-" + district.name());
                } catch (Exception e){
                    System.out.println("Error while warming up: " + standardKpi.kpiName() + "-" + district.name());
                }
            }
        }
    }
}
