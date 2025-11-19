package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CacheWarmupAsyncServiceImpl implements CacheWarmupAsyncService {

    private final KpiDayService kpiDayService;
    private final BasicKpiService basicKpiService;
    private final DistrictService districtService;
    private final StandardKpiService standardKpiService;
    private final String[] periods = {"day"};
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final CellNameService cellNameService;
//    private final DateService dateService;


    @Override
    @Async
    public CompletableFuture<Void> warmupBasicKpiSnapshotCache(String ratName) {
        System.out.println("Warming-up Basic KPI cache of: " + ratName + "...");
        List<BasicKpiDto> basicKpis = basicKpiService.getAllByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();
        for (String period : periods) {
            for (BasicKpiDto dto : basicKpis) {
                try {
                    kpiDayService.getLatestBasicAndStandardKpiSnapshots(dto.kpiName(), period, ratName);
                    System.out.println("[" + ratName + "] Cache BasicKpi warmed-up: " + dto.kpiName() + "-" + period);
                } catch (Exception e) {
                    System.out.println("[" + ratName + "] Error warming cache for: " + dto.kpiName() + "-" + period);
                    e.printStackTrace();
                }
                for (DistrictDto district : districts) {
                    try {
                        kpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(dto.kpiName(), period, district.name(), ratName);
                        System.out.println("[" + ratName + "] Cache BasicKpi warmed-up: " + dto.kpiName() + "-" + period + "-" + district.name());
                    } catch (Exception e) {
                        System.out.println("[" + ratName + "] Error warming cache for: " + dto.kpiName() + "-" + period + "-" + district.name());
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Warming-up Basic KPI cache of: " + ratName + " is complete!");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> warmupKpiTrendCache(String ratName) {
        System.out.println("Warming-up KPI trend cache of: " + ratName + "...");
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                kpiDayService.getTrendByKpi(standardKpi.kpiName(), "month", ratName);
                System.out.println("[" + ratName + "] Cache KPI trend warmed-up (month): " + standardKpi.kpiName());
            } catch (Exception e) {
                System.out.println("[" + ratName + "] Error while warming KPI trend cache for (month): " + standardKpi.kpiName());
            }
            for (DistrictDto district : districts) {
                try {
                    kpiDayService.getTrendByKpiAndDistrict(standardKpi.kpiName(), "month", district.name(), ratName);
                    System.out.println("[" + ratName + "] Cache KPI trend warmed-up (month): " + standardKpi.kpiName() + "-" + district.name());
                } catch (Exception e) {
                    System.out.println("[" + ratName + "] Error while warming KPI trend cache for (month): " + standardKpi.kpiName() + "-" + district.name());
                }
            }
        }
        System.out.println("Warming-up KPI trend cache of: " + ratName + " is complete!");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> warmupWorstCellCache(String ratName) {
        System.out.println("Warming-up Worst-cell cache of: " + ratName + "...");
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                kpiDayService.getWorstCellsByKpi(standardKpi.kpiName(), "day", ratName);
                System.out.println("[" + ratName + "] Cache Worst cells warmed-up: " + standardKpi.kpiName());
            } catch (Exception e) {
                System.out.println("[" + ratName + "] Error while warming Worst cell cache for: " + standardKpi.kpiName());
            }

            for (DistrictDto district : districts) {
                try {
                    kpiDayService.getWorstCellsByKpiAndDistrict(standardKpi.kpiName(), "day", district.name(), ratName);
                    System.out.println("[" + ratName + "] Cache Worst cells warmed-up: " + standardKpi.kpiName() + "-" + district.name());
                } catch (Exception e) {
                    System.out.println("[" + ratName + "] Error while warming up worst cells cache : " + standardKpi.kpiName() + "-" + district.name());
                }
            }
        }
        System.out.println("Warming-up Worst cell cache of: " + ratName + " is complete!");
        return CompletableFuture.completedFuture(null);
    }
}
