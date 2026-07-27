package dev.thilanka.netrics.config;

import dev.thilanka.netrics.dto.DateRangeDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
        System.out.println(">>> MY cacheManager building, serializer=" + valueSerializer.getClass());

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
        System.out.println(">>> valueSerializer bean created, defaultTyping="
                + (mapper.getSerializationConfig().getDefaultTyper(null) != null));
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
}
