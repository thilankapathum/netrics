package dev.thilanka.netrics.service;

public interface CacheWarmup {
    void evictAndWarmupCache(String ratName, String granularityName);

    void evictByRatName(String ratName);

    int evictByRatNameAndGranularityName(String ratName, String granularityName);

    void warmUpCache(String ratName, String granularityName);
}
