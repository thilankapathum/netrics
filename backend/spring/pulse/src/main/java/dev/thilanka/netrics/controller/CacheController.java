package dev.thilanka.netrics.controller;

import dev.thilanka.netrics.service.CacheWarmup;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("evict-all")
    public ResponseEntity<String> evictAllCaches() {
        Integer cacheCount = 0;
        StringBuilder evictedCaches = new StringBuilder("All caches cleared! ");
        for (String name : cacheManager.getCacheNames()) {
            cacheManager.getCache(name).clear();
            System.out.println("Cleared cache: " + name);
            evictedCaches.append(name).append(", ");
            cacheCount++;
        }
        return ResponseEntity.ok(cacheCount.toString());
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("evict")
    public ResponseEntity<String> evictByRatName(@RequestParam("ratName") String ratName) {
        cacheWarmup.evictByRatName(ratName);
        return ResponseEntity.ok("Cache of " + ratName + " is cleared!");
    }

//    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("evict-rat-granularity")
    public ResponseEntity<String> evictByRatAndGranularity(@RequestParam("ratName") String ratName, @RequestParam("granularityName") String granularityName) {
        int cleared = cacheWarmup.evictByRatNameAndGranularityName(ratName, granularityName);
        return ResponseEntity.ok("[" + ratName + " - " + granularityName + "] Evicted " + cleared + " cache entries!");
    }

    //    @PreAuthorize("hasAuthority('ROLE_PULSE_UPDATE')")
    @PostMapping("evict-and-warmup")        //TODO: Implement in Python Script
    public ResponseEntity<String> evictAndWarmupCaches(@RequestParam("ratName") String ratName, @RequestParam("granularityName") String granularityName) {
        System.out.println("Clearing and warming-up cache for: " + ratName + " - " + granularityName);
        cacheWarmup.evictAndWarmupCache(ratName, granularityName);
        return ResponseEntity.ok(ratName + " caches cleared and warmed-up!");
    }

    @PreAuthorize("hasAuthority('ROLE_PULSE_CREATE')")
    @PostMapping("warm-up")
    public ResponseEntity<String> warmUpCaches(@RequestParam("ratName") String ratName, @RequestParam("granularityName") String granularityName) {
        cacheWarmup.warmUpCache(ratName, granularityName);
        return ResponseEntity.ok(ratName + " caches warmed-up!");
    }

}
