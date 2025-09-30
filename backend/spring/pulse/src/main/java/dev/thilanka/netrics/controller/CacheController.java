package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.service.CacheWarmup;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/cache")
@RequiredArgsConstructor
public class CacheController {
    private final CacheManager cacheManager;
    private final CacheWarmup cacheWarmup;

    @PostMapping("evict-all")
    public ResponseEntity<String> evictAllCaches(){
        for (String name : cacheManager.getCacheNames()){
            cacheManager.getCache(name).clear();
        }
        return ResponseEntity.ok("All caches cleared!");
    }

    @PostMapping("evict-and-warmup")
    public ResponseEntity<String> evictAndWarmupCaches(){
        for (String name: cacheManager.getCacheNames()){
            cacheManager.getCache(name).clear();
        }
        cacheWarmup.warmupCache();
        return ResponseEntity.ok("All caches cleared and warmed-up!");

    }

}
