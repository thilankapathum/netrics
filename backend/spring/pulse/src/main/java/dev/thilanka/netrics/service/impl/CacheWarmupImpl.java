package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BasicKpiDto;
import dev.thilanka.netrics.dto.DistrictDto;
import dev.thilanka.netrics.dto.StandardKpiDto;
import dev.thilanka.netrics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CacheWarmupImpl implements CacheWarmup {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CellNameService cellNameService;
    private final DateService dateService;
    private final CacheWarmupAsyncService cacheWarmupAsyncService;
    private final CellService cellService;


    @Override
    public void evictAndWarmupCache(String ratName, String granularityName) {
        long start = System.currentTimeMillis();
//        evictByRatName(ratName);
        evictByRatNameAndGranularityName(ratName, granularityName);
        warmupDateRangeCache(ratName, granularityName);

        CompletableFuture<Integer> reloadCellsFuture = cellNameService.reloadCells(ratName,granularityName);
        CompletableFuture<Void> basicKpiSnapshotFuture = cacheWarmupAsyncService.warmupBasicKpiSnapshotCache(ratName, granularityName);
        CompletableFuture<Void> kpiTrendFuture = cacheWarmupAsyncService.warmupKpiTrendCache(ratName, granularityName);
        CompletableFuture<Void> worstCellFuture = cacheWarmupAsyncService.warmupWorstCellCache(ratName, granularityName);

        CompletableFuture.allOf(reloadCellsFuture, basicKpiSnapshotFuture, kpiTrendFuture, worstCellFuture).join();
//        CompletableFuture.allOf(basicKpiSnapshotFuture).join();


        long end = System.currentTimeMillis();
        long difference = end - start;
        Duration duration = Duration.ofMillis(difference);
        String formatted = String.format("%02dh %02dm %02ds", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
        System.out.println("[" + ratName + "] ---- CACHE WARM-UP COMPLETE! - took " + formatted + " ----");
    }

    @Override
    public void evictByRatName(String ratName) {
        String pattern = "*_" + ratName;
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("[" + ratName + "] Evicted " + keys.size() + " cache entries!");
        } else {
            System.out.println("[" + ratName + "] No cache entries found!");
        }
    }

    @Override
    public int evictByRatNameAndGranularityName(String ratName, String granularityName) {
        String pattern = "*_" + granularityName + "_" + ratName;
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("[" + ratName + " - " + granularityName + "] Evicted " + keys.size() + " cache entries!");
            cellNameService.reloadCells(ratName, granularityName);
            cellService.findCellCountWithMissingInfo();
            return keys.size();
        } else {
            System.out.println("[" + ratName + " - " + granularityName + "] No cache entries found!");
            cellNameService.reloadCells(ratName, granularityName);
            cellService.findCellCountWithMissingInfo();
            return 0;
        }
    }

    @Override
    public void warmUpCache(String ratName, String granularityName) {
        warmupDateRangeCache(ratName, granularityName);

        CompletableFuture<Integer> reloadCellsFuture = cellNameService.reloadCells(ratName, granularityName);
        CompletableFuture<Void> basicKpiSnapshotFuture = cacheWarmupAsyncService.warmupBasicKpiSnapshotCache(ratName, granularityName);
        CompletableFuture<Void> kpiTrendFuture = cacheWarmupAsyncService.warmupKpiTrendCache(ratName, granularityName);
        CompletableFuture<Void> worstCellFuture = cacheWarmupAsyncService.warmupWorstCellCache(ratName, granularityName);

        CompletableFuture.allOf(reloadCellsFuture, basicKpiSnapshotFuture, kpiTrendFuture, worstCellFuture).join();
        System.out.println("[" + ratName + " - " + granularityName + "] CACHE WARM-UP COMPLETE!");
    }

    private void warmupDateRangeCache(String ratName, String granularityName) {
        String[] aggregationList = {"day", "week", "month"};

        for (String aggregation : aggregationList) {
            dateService.getLatestDateRange(aggregation, ratName, granularityName);
        }
    }
}
