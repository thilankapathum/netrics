package dev.thilanka.netrics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

//    @Value("${caching.async.core-pool-size}")
//    private int corePoolSize;
//
//    @Value("${caching.async.max-pool-size}")
//    private int maxPoolSize;
//
//    @Value("${caching.async.queue-capacity}")
//    private int queueCapacity;


    @Bean(name = "cacheExecutor")
    public Executor cacheExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(9);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("CacheWarmup-");
        executor.initialize();
        return executor;
    }
}
