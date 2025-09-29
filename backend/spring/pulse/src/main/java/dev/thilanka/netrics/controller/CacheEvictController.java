package dev.thilanka.netrics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pulse/cache")
@RequiredArgsConstructor
public class CacheEvictController {
    private final CacheManager cacheManager;

    @PostMapping("evict-all")
    public ResponseEntity<String> evictAllCaches(){
        for (String name : cacheManager.getCacheNames()){
            cacheManager.getCache(name).clear();
        }

        return ResponseEntity.ok("All caches cleared!");
    }

}
