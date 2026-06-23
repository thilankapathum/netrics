package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.dto.SectorEvent;
import dev.thilanka.beam.dto.SiteEvent;
import dev.thilanka.beam.service.BeamEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeamEventPublisherImpl implements BeamEventPublisher {
//    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.site-events}")
    private String siteEventsTopic;

    @Value("${spring.kafka.topic.sector-events}")
    private String sectorEventsTopic;

    @Override
    public void publishSiteEvent(SiteEvent event) {
    // key = siteCode() → ensures all events for the same site land on the same partition (preserves ordering)
        kafkaTemplate.send(siteEventsTopic, event.siteCode(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish SiteEvent id={}", event.siteCode(), ex);
                    } else {
                        log.debug("Published SiteEvent id={} offset={}",
                                event.siteCode(), result.getRecordMetadata().offset());
                    }
                });
    }

    @Override
    public void publishSectorEvent(SectorEvent event) {
        kafkaTemplate.send(sectorEventsTopic, event.name(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish SectorEvent id={}", event.name(), ex);
                    } else {
                        log.debug("Published SectorEvent id={} offset={}",
                                event.name(), result.getRecordMetadata().offset());
                    }
                });

    }
}
