package dev.thilanka.netrics.service;

public interface CacheWarmup {
    void evictAndWarmupCache(String ratName);

    void evictByRatName(String ratName);
}
