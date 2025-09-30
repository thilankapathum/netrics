package dev.thilanka.netrics.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

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
    RedisSerializer<Object> valueSerializer(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

//        return new GenericJackson2JsonRedisSerializer(objectMapper);
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory){

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()))
                .entryTtl(Duration.ofHours(23));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .transactionAware()
                .build();
    }
}
