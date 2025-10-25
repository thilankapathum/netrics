package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CacheWarmupImpl implements CacheWarmup {
    private final KpiDayService kpiDayService;
    private final BasicKpiService basicKpiService;
    private final DistrictService districtService;
    private final StandardKpiService standardKpiService;
    private final String[] periods = {"day"};
    private final RedisTemplate<String, Object> redisTemplate;
    private final CellNameService cellNameService;


    @Override
    public void evictAndWarmupCache(String ratName) {
        evictByRatName(ratName);
        warmupBasicKpiSnapshotCache(ratName);
        warmupKpiTrendCache(ratName);
        warmupWorstCellCache(ratName);
        cellNameService.reloadCells();
        System.out.println("Cache warmup complete for: " + ratName + "!");
    }

    @Override
    public void evictByRatName(String ratName) {
        String pattern = "*_"+ratName;
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()){
            redisTemplate.delete(keys);
            System.out.println("Evicted " + keys.size() + " cache entries for RAT: " + ratName);
        } else {
            System.out.println("No cache entries found for RAT: " + ratName);
        }
    }

    private void warmupBasicKpiSnapshotCache(String ratName) {
        System.out.println("Warming-up Basic KPI cache of: " + ratName + "...");
        List<BasicKpiDto> basicKpis = basicKpiService.getAllByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();
        for (String period : periods) {
            for (BasicKpiDto dto : basicKpis) {
                try {
                    kpiDayService.getLatestBasicAndStandardKpiSnapshots(dto.kpiName(), period, ratName);
                    System.out.println("Cache BasicKpi warmed-up: " + dto.kpiName() + "-" + period);
                } catch (Exception e) {
                    System.out.println("Error warming cache for: " + dto.kpiName() + "-" + period);
//                    e.printStackTrace();
                }
                for (DistrictDto district : districts) {
                    try {
                        kpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(dto.kpiName(), period, district.name(), ratName);
                        System.out.println("Cache BasicKpi warmed-up: " + dto.kpiName() + "-" + period + "-" + district.name());
                    } catch (Exception e) {
                        System.out.println("Error warming cache for: " + dto.kpiName() + "-" + period + "-" + district.name());
//                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Warming-up Basic KPI cache of: " + ratName + " is complete!");
    }

    private void warmupKpiTrendCache(String ratName) {
        System.out.println("Warming-up KPI trend cache of: " + ratName + "...");
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                kpiDayService.getTrendByKpi(standardKpi.kpiName(), "month", ratName);
                System.out.println("Cache KPI trend warmed-up (month): " + standardKpi.kpiName());
            } catch (Exception e) {
                System.out.println("Error while warming KPI trend cache for (month): " + standardKpi.kpiName());
            }
            for (DistrictDto district : districts) {
                try {
                    kpiDayService.getTrendByKpiAndDistrict(standardKpi.kpiName(), "month", district.name(), ratName);
                    System.out.println("Cache KPI trend warmed-up (month): " + standardKpi.kpiName() + "-" + district.name());
                } catch (Exception e) {
                    System.out.println("Error while warming KPI trend cache for (month): " + standardKpi.kpiName() + "-" + district.name());
                }
            }
        }
        System.out.println("Warming-up KPI trend cache of: " + ratName + " is complete!");
    }

    private void warmupWorstCellCache(String ratName) {
        System.out.println("Warming-up Worst-cell cache of: " + ratName + "...");
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                kpiDayService.getWorstCellsByKpi(standardKpi.kpiName(), "day", ratName);
                System.out.println("Cache Worst cells warmed-up: " + standardKpi.kpiName());
            } catch (Exception e) {
                System.out.println("Error while warming Worst cell cache for: " + standardKpi.kpiName());
            }

            for (DistrictDto district : districts) {
                try {
                    kpiDayService.getWorstCellsByKpiAndDistrict(standardKpi.kpiName(), "day", district.name(), ratName);
                    System.out.println("Cache Worst cells warmed-up: " + standardKpi.kpiName() + "-" + district.name());
                } catch (Exception e) {
                    System.out.println("Error while warming up worst cells cache : " + standardKpi.kpiName() + "-" + district.name());
                }
            }
        }
        System.out.println("Warming-up Worst cell cache of: " + ratName + " is complete!");
    }
}
