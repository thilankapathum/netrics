package dev.thilanka.netrics.service;

public interface CacheWarmup {
    void evictAndWarmupCache(String ratName, String granularityName);

    void evictByRatName(String ratName);

    void warmUpCache(String ratName, String granularityName);
}
