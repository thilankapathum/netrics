package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.entity.ltefdd.LteFddBasicKpi;
import dev.thilanka.netrics.service.CacheWarmup;
import dev.thilanka.netrics.service.DistrictService;
import dev.thilanka.netrics.service.LteFddBasicKpiService;
import dev.thilanka.netrics.service.LteFddKpiDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CacheWarmupImpl implements CacheWarmup {
    private final LteFddKpiDayService lteFddKpiDayService;
    private final LteFddBasicKpiService lteFddBasicKpiService;
    private final DistrictService districtService;
    private final String[] periods = {"day", "week"};

    @Override
    public void warmupCache() {
        warmupLteFddBasicKpiSnapshotCache();
        System.out.println("Cache warmup complete!");
    }

    private void warmupLteFddBasicKpiSnapshotCache() {
        List<BasicKpiDto> basicKpis = lteFddBasicKpiService.getAll();
        List<DistrictDto> districts = districtService.getAll();
        for (String period : periods) {
            for (BasicKpiDto dto : basicKpis) {
                try {
                    lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshots(dto.kpiName(), period);
                    System.out.println("Cache warmed-up: " + dto.kpiName() + period);
                } catch (Exception e) {
                    System.out.println("Error warming cache for: " + dto.kpiName() + period);
//                    e.printStackTrace();
                }
                for (DistrictDto district : districts) {
                    try {
                        lteFddKpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(dto.kpiName(), period, district.name());
                        System.out.println("Cache warmed-up: " + dto.kpiName() + period + district.name());
                    } catch (Exception e) {
                        System.out.println("Error warming cache for: " + dto.kpiName() + period + district.name());
//                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
