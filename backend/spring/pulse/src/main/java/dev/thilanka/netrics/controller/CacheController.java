package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.service.CacheWarmup;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            System.out.println("Cleared cache: " + name);
        }
        return ResponseEntity.ok("All caches cleared!");
    }

    @PostMapping("evict")
    public ResponseEntity<String> evictByRatName(@RequestParam("ratName") String ratName){
        cacheWarmup.evictByRatName(ratName);
        return ResponseEntity.ok("Cache of " + ratName + " is cleared!");
    }

    @PostMapping("evict-and-warmup")
    public ResponseEntity<String> evictAndWarmupCaches(@RequestParam("ratName") String ratName){
        cacheWarmup.evictAndWarmupCache(ratName);
        return ResponseEntity.ok(ratName + " caches cleared and warmed-up!");
    }

}
