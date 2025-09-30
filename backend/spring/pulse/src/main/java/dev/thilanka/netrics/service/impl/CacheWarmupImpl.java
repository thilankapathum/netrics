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
    private final LteFddKpiDayService lteFddKpiDayService;
    private final LteFddBasicKpiService lteFddBasicKpiService;
    private final DistrictService districtService;
    private final LteFddStandardKpiService lteFddStandardKpiService;
    private final String[] periods = {"day"};


    @Override
    public void warmupCache() {
        warmupLteFddBasicKpiSnapshotCache();
        warmupLteFddKpiTrendCache();
        System.out.println("Cache warmup complete!");
    }

    private void warmupLteFddBasicKpiSnapshotCache() {
        List<BasicKpiDto> basicKpis = lteFddBasicKpiService.getAll();
        List<DistrictDto> districts = districtService.getAll();
        for (String period : periods) {
            for (BasicKpiDto dto : basicKpis) {
                try {
                    lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshots(dto.kpiName(), period);
                    System.out.println("Cache LteFddBasicKpi warmed-up: " + dto.kpiName() + period);
                } catch (Exception e) {
                    System.out.println("Error warming cache for: " + dto.kpiName() + period);
//                    e.printStackTrace();
                }
                for (DistrictDto district : districts) {
                    try {
                        lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(dto.kpiName(), period, district.name());
                        System.out.println("Cache LteFddBasicKpi warmed-up: " + dto.kpiName() + period + district.name());
                    } catch (Exception e) {
                        System.out.println("Error warming cache for: " + dto.kpiName() + period + district.name());
//                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void warmupLteFddKpiTrendCache() {
        List<StandardKpiDto> lteFddStandardKpis = lteFddStandardKpiService.getAllStandardKpi();
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                lteFddKpiDayService.getTrendByKpi(standardKpi.kpiName(), "month");
                System.out.println("Cache LteFddKpiTrend warmed-up (month): " + standardKpi.kpiName());
            } catch (Exception e) {
                System.out.println("Error while warming cache for (month): " + standardKpi.kpiName());
            }
            for (DistrictDto district : districts) {
                try {
                    lteFddKpiDayService.getTrendByKpiAndDistrict(standardKpi.kpiName(), "month", district.name());
                    System.out.println("Cache LteFddKpiTrend warmed-up (month): " + standardKpi.kpiName() + district.name());
                } catch (Exception e) {
                    System.out.println("Error while warming cache for (month): " + standardKpi.kpiName() + district.name());
                }
            }
        }
    }
}
