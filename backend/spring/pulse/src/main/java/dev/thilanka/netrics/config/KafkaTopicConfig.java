package dev.thilanka.netrics.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {


    @Value("${spring.kafka.topic.site-events}")
    private String siteEventsTopic;

    @Value("${spring.kafka.topic.sector-events}")
    private String sectorEventsTopic;

    @Value("${spring.kafka.topic.band-events}")
    private String bandEventsTopic;

    @Bean
    public NewTopic siteEventsTopic() {
        return TopicBuilder.name(siteEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sectorEventsTopic() {
        return TopicBuilder.name(sectorEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bandEventsTopic() {
        return TopicBuilder.name(bandEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
