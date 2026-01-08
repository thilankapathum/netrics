package dev.thilanka.netrics.service;

import java.util.concurrent.CompletableFuture;

public interface CacheWarmupAsyncService {
    CompletableFuture<Void> warmupBasicKpiSnapshotCache(String ratName, String granularityName);
    CompletableFuture<Void> warmupKpiTrendCache(String ratName, String granularityName);
    CompletableFuture<Void> warmupWorstCellCache(String ratName, String granularityName);
}
