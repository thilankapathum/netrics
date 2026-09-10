package dev.thilanka.netrics.config;

import dev.thilanka.netrics.dto.AnomalySummaryDto;
import dev.thilanka.netrics.dto.DateRangeDto;
import dev.thilanka.netrics.dto.PagedResponse;
import dev.thilanka.netrics.dto.WorstCellsDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    RedisSerializer<String> keySerializer(){
        return new StringRedisSerializer();
    }

//    @Bean
//    RedisSerializer<Object> valueSerializer(){
//        return new GenericJackson2JsonRedisSerializer();
//    }

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                   RedisSerializer<Object> valueSerializer) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .entryTtl(Duration.ofHours(23));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(
                "mapCellTiles",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                        .entryTtl(Duration.ofHours(1))
        );
        cacheConfigs.put(
                "latestDateRange",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(dateRangeSerializer()))
                        .entryTtl(Duration.ofHours(23))
        );
        // WorstCellsDto/PagedResponse/AnomalySummaryDto are final records, so NON_FINAL default
        // typing (valueSerializer()) omits @class metadata for them, and a cache hit deserializes
        // to LinkedHashMap instead of the record -> ClassCastException on the second load.
        // Type-bound serializers (like dateRangeSerializer() above) sidestep that entirely.
        cacheConfigs.put(
                "anomalyCells",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(anomalyCellsSerializer()))
                        .entryTtl(Duration.ofHours(23))
        );
        cacheConfigs.put(
                "allAnomalyCellsByArea",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(pagedWorstCellsSerializer()))
                        .entryTtl(Duration.ofHours(23))
        );
        cacheConfigs.put(
                "anomalySummaryByArea",
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(anomalySummarySerializer()))
                        .entryTtl(Duration.ofHours(23))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .transactionAware()
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value serializer
        template.setValueSerializer(valueSerializer());
        template.setHashValueSerializer(valueSerializer());

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    RedisSerializer<Object> valueSerializer(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    RedisSerializer<Object> dateRangeSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // no default typing — type is fixed by the serializer's target class
        Jackson2JsonRedisSerializer<DateRangeDto> ser =
                new Jackson2JsonRedisSerializer<>(mapper, DateRangeDto.class);
        return (RedisSerializer) ser;
    }

    private ObjectMapper noDefaultTypingMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    RedisSerializer<Object> anomalyCellsSerializer() {
        ObjectMapper mapper = noDefaultTypingMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, WorstCellsDto.class);
        Jackson2JsonRedisSerializer<Object> ser = new Jackson2JsonRedisSerializer<>(mapper, type);
        return ser;
    }

    @Bean
    RedisSerializer<Object> pagedWorstCellsSerializer() {
        ObjectMapper mapper = noDefaultTypingMapper();
        JavaType type = mapper.getTypeFactory().constructParametricType(PagedResponse.class, WorstCellsDto.class);
        Jackson2JsonRedisSerializer<Object> ser = new Jackson2JsonRedisSerializer<>(mapper, type);
        return ser;
    }

    @Bean
    RedisSerializer<Object> anomalySummarySerializer() {
        ObjectMapper mapper = noDefaultTypingMapper();
        Jackson2JsonRedisSerializer<AnomalySummaryDto> ser =
                new Jackson2JsonRedisSerializer<>(mapper, AnomalySummaryDto.class);
        return (RedisSerializer) ser;
    }
}
