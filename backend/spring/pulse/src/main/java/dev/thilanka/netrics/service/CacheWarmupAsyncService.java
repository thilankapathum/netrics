package dev.thilanka.netrics.service;

import java.util.concurrent.CompletableFuture;

public interface CacheWarmupAsyncService {
    CompletableFuture<Void> warmupBasicKpiSnapshotCache(String ratName);
    CompletableFuture<Void> warmupKpiTrendCache(String ratName);
    CompletableFuture<Void> warmupWorstCellCache(String ratName);
}
