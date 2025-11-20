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


    @Override
    public void evictAndWarmupCache(String ratName) {
        long start = System.currentTimeMillis();
        evictByRatName(ratName);
        warmupDateRangeCache(ratName);

        CompletableFuture<Integer> reloadCellsFuture = cellNameService.reloadCells();
        CompletableFuture<Void> basicKpiSnapshotFuture = cacheWarmupAsyncService.warmupBasicKpiSnapshotCache(ratName);
        CompletableFuture<Void> kpiTrendFuture = cacheWarmupAsyncService.warmupKpiTrendCache(ratName);
        CompletableFuture<Void> worstCellFuture = cacheWarmupAsyncService.warmupWorstCellCache(ratName);

        CompletableFuture.allOf(reloadCellsFuture,basicKpiSnapshotFuture,kpiTrendFuture,worstCellFuture).join();

        long end = System.currentTimeMillis();
        long difference = end - start;
        Duration duration = Duration.ofMillis(difference);
        String formatted = String.format("%02dh %02dm %02ds", duration.toHours(),duration.toMinutesPart(),duration.toSecondsPart());
        System.out.println("CACHE WARM-UP COMPLETE FOR: " + ratName.toUpperCase() + "! | Took " + formatted);
    }

    @Override
    public void evictByRatName(String ratName) {
        String pattern = "*_" + ratName;
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            System.out.println("Evicted " + keys.size() + " cache entries for RAT: " + ratName);
        } else {
            System.out.println("No cache entries found for RAT: " + ratName);
        }
    }

    @Override
    public void warmUpCache(String ratName) {
        warmupDateRangeCache(ratName);

        CompletableFuture<Integer> reloadCellsFuture = cellNameService.reloadCells();
        CompletableFuture<Void> basicKpiSnapshotFuture = cacheWarmupAsyncService.warmupBasicKpiSnapshotCache(ratName);
        CompletableFuture<Void> kpiTrendFuture = cacheWarmupAsyncService.warmupKpiTrendCache(ratName);
        CompletableFuture<Void> worstCellFuture = cacheWarmupAsyncService.warmupWorstCellCache(ratName);

        CompletableFuture.allOf(reloadCellsFuture, basicKpiSnapshotFuture,kpiTrendFuture,worstCellFuture).join();
        System.out.println("CACHE WARM-UP COMPLETE FOR: " + ratName.toUpperCase() + "!");
    }

    private void warmupDateRangeCache(String ratName) {
        String[] granularityList = {"day", "week", "month"};

        for (String granularity : granularityList) {
            dateService.getLatestDateRange(granularity, ratName);
        }
    }
}
