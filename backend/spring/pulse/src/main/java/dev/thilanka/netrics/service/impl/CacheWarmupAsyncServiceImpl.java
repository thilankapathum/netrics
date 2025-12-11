package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.service.*;
import dev.thilanka.netrics.util.ConsoleProgress;
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

    private final ConsoleProgress consoleProgress;


    @Override
    @Async("cacheExecutor")
    public CompletableFuture<Void> warmupBasicKpiSnapshotCache(String ratName, String granularityName) {
        System.out.println("[" + ratName + "] Started warming-up Basic KPI cache...");
        List<BasicKpiDto> basicKpis = basicKpiService.getAllByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

//        consoleProgress.setBasicKpiTotal(periods.length * basicKpis.size() * (districts.size() + 1)); // +1 for district-free call
//        consoleProgress.setBasicKpiCompleted(0);
//        consoleProgress.setRatName(ratName);
//        Integer basicKpiTotal = periods.length * basicKpis.size() * (districts.size() + 1); // +1 for district-free call
//        consoleProgress.setBasicKpiTotalMap(ratName, basicKpiTotal);
//        consoleProgress.setBasicKpiCompletedMap(ratName, 0);


        for (String period : periods) {
            for (BasicKpiDto dto : basicKpis) {
                try {
                    kpiDayService.getLatestBasicAndStandardKpiSnapshots(dto.kpiName(), period, ratName,granularityName);
                } catch (Exception e) {
                    System.out.println("[" + ratName + "] Error warming cache for: " + dto.kpiName() + "-" + period);
                }

//                consoleProgress.setBasicKpiCompleted(consoleProgress.getBasicKpiCompleted()+1);
//                consoleProgress.setBasicKpiCompletedMap(ratName, consoleProgress.getBasicKpiCompletedMap().get(ratName) + 1);
                for (DistrictDto district : districts) {
                    try {
                        kpiDayService.getLatestBasicAndStandardKpiSnapshotsWithDistrict(dto.kpiName(), period, district.name(), ratName,granularityName);
                    } catch (Exception e) {
                        System.out.println("[" + ratName + "] Error warming cache for: " + dto.kpiName() + "-" + period + "-" + district.name());
                    }
//                    consoleProgress.setBasicKpiCompleted(consoleProgress.getBasicKpiCompleted()+1);
//                    consoleProgress.setBasicKpiCompletedMap(ratName, consoleProgress.getBasicKpiCompletedMap().get(ratName) + 1);
                }
                System.out.println("[" + ratName + "] Basic KPI cache is warmed up for: " + dto.label());
            }
        }
        System.out.println("[" + ratName + "] -- BASIC KPI CACHE IS WARMED-UP! --");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("cacheExecutor")
    public CompletableFuture<Void> warmupKpiTrendCache(String ratName, String granularityName) {
        System.out.println("[" + ratName + "] Started warming-up KPI trend cache...");
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

//        consoleProgress.setKpiTrendTotal(lteFddStandardKpis.size() * (districts.size() + 1)); // +1 for district-free call
//        int completedTasks = 0;
//        consoleProgress.setKpiTrendCompleted(0);
//        consoleProgress.setRatName(ratName);

//        Integer kpiTrendTotal = lteFddStandardKpis.size() * (districts.size() + 1);
//        consoleProgress.setKpiTrendTotalMap(ratName, kpiTrendTotal);
//        consoleProgress.setKpiTrendCompletedMap(ratName, 0);


        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                kpiDayService.getTrendByKpi(standardKpi.kpiName(), "month", ratName, granularityName);
            } catch (Exception e) {
                System.out.println("[" + ratName + "] Error while warming KPI trend cache for (month): " + standardKpi.kpiName());
            }

//            completedTasks++;
//            consoleProgress.setKpiTrendCompleted(consoleProgress.getKpiTrendCompleted() + 1);
//            consoleProgress.setKpiTrendCompletedMap(ratName, consoleProgress.getKpiTrendCompletedMap().get(ratName) + 1);
            for (DistrictDto district : districts) {
                try {
                    kpiDayService.getTrendByKpiAndDistrict(standardKpi.kpiName(), "month", district.name(), ratName, granularityName);
                } catch (Exception e) {
                    System.out.println("[" + ratName + "] Error while warming KPI trend cache for (month): " + standardKpi.kpiName() + "-" + district.name());
                }

//                completedTasks++;
//                consoleProgress.setKpiTrendCompletedMap(ratName, consoleProgress.getKpiTrendCompletedMap().get(ratName) + 1);
            }
            System.out.println("[" + ratName + "] KPI Trend cache is warmed up for: " + standardKpi.label());

        }
        System.out.println("[" + ratName + "] -- KPI TREND CACHE IS WARMED-UP! --");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async("cacheExecutor")
    public CompletableFuture<Void> warmupWorstCellCache(String ratName, String granularityName) {
        System.out.println("[" + ratName + "] Started warming-up Worst cell cache...");
        List<StandardKpiDto> lteFddStandardKpis = standardKpiService.getAllStandardKpiByRat(ratName);
        List<DistrictDto> districts = districtService.getAll();

//        consoleProgress.setWorstCellTotal(lteFddStandardKpis.size() * (districts.size() + 1)); // +1 for district-free call
//        int completedTasks = 0;
//        consoleProgress.setWorstCellCompleted(0);
//        consoleProgress.setRatName(ratName);

//        Integer worstCellTotal = lteFddStandardKpis.size() * (districts.size() + 1); // +1 for district-free call
//        consoleProgress.setWorstCellTotalMap(ratName, worstCellTotal);
//        consoleProgress.setWorstCellCompletedMap(ratName, 0);


        for (StandardKpiDto standardKpi : lteFddStandardKpis) {
            try {
                kpiDayService.getWorstCellsByKpi(standardKpi.kpiName(), "day", false, ratName, granularityName);
            } catch (Exception e) {
                System.out.println("[" + ratName + "] Error while warming Worst cell cache for: " + standardKpi.kpiName());
            }

//            completedTasks++;
//            consoleProgress.setWorstCellCompletedMap(ratName, consoleProgress.getWorstCellCompletedMap().get(ratName) + 1);

            for (DistrictDto district : districts) {
                try {
                    kpiDayService.getWorstCellsByKpiAndDistrict(standardKpi.kpiName(), "day", false, district.name(), ratName,granularityName);
                } catch (Exception e) {
                    System.out.println("[" + ratName + "] Error while warming up worst cells cache : " + standardKpi.kpiName() + "-" + district.name());
                }

//                completedTasks++;
//                consoleProgress.setWorstCellCompletedMap(ratName, consoleProgress.getWorstCellCompletedMap().get(ratName) + 1);
            }
            System.out.println("[" + ratName + "] Worst Cell cache is warmed up for: " + standardKpi.label());
        }
        System.out.println("[" + ratName + "] -- WORST CELL CACHE IS WARMED-UP! --");
        return CompletableFuture.completedFuture(null);
    }
}
