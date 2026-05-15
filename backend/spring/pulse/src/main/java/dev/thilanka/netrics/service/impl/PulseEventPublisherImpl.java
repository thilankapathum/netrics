package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.BandEvent;
import dev.thilanka.netrics.service.PulseEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PulseEventPublisherImpl implements PulseEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

//    @Value("${spring.kafka.topic.site-events}")
//    private String siteEventsTopic;
//
//    @Value("${spring.kafka.topic.sector-events}")
//    private String sectorEventsTopic;

    @Value("${spring.kafka.topic.band-events}")
    private String bandEventsTopic;


//    @Override
//    public void publishSiteEvent(SiteEvent event) {
//        // key = siteId.toString() → ensures all events for the same site land on the same partition (preserves ordering)
//        kafkaTemplate.send(siteEventsTopic, event.id().toString(), event)
//                .whenComplete((result, ex) -> {
//                    if (ex != null) {
//                        log.error("Failed to publish SiteEvent id={}", event.id(), ex);
//                    } else {
//                        log.debug("Published SiteEvent id={} offset={}",
//                                event.id(), result.getRecordMetadata().offset());
//                    }
//                });
//    }
//
//    @Override
//    public void publishSectorEvent(SectorEvent event) {
//        kafkaTemplate.send(sectorEventsTopic, event.id().toString(), event)
//                .whenComplete((result, ex) -> {
//                    if (ex != null) {
//                        log.error("Failed to publish SectorEvent id={}", event.id(), ex);
//                    } else {
//                        log.debug("Published SectorEvent id={} offset={}",
//                                event.id(), result.getRecordMetadata().offset());
//                    }
//                });
//
//    }

    @Override
    public void publishBandEvent(BandEvent event) {
        kafkaTemplate.send(bandEventsTopic, event.id().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BandEvent id={}", event.id(), ex);
                    } else {
                        log.debug("Published BandEvent id={} offset={}",
                                event.id(), result.getRecordMetadata().offset());
                    }
                });
    }
}
