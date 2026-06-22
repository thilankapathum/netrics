package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.dto.BandEvent;
import dev.thilanka.beam.repository.BeamBandRepository;
import dev.thilanka.beam.service.PulseSyncConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class PulseSyncConsumerImpl implements PulseSyncConsumer {

    private final BeamBandRepository beamBandRepository;

    @Override
    @KafkaListener(
            topics = "${spring.kafka.topic.band-events}",
            groupId = "beam-service",
            containerFactory = "bandEventListenerContainerFactory"
    )
    public void handleBandEvent(BandEvent event) {
        log.info("Received BandEvent type={} id={} BandName={}", event.eventType(), event.id(), event.name());
        try {
            switch (event.eventType()) {
                case "CREATED", "UPDATED" -> upsertBand(event);
                case "DELETED" -> beamBandRepository.deleteById(event.id());
                default -> log.warn("Unknown Band event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Error processing BandEvent id={}", event.id(), e);
            throw e;
        }
    }

    private void upsertBand(BandEvent event) {
        //TODO: Create Producer
        beamBandRepository.upsert(event.id(), event.name(), event.number(), event.unit(),
                event.createdAt(), event.createdBy());
        log.info("Upserted band {} - {}", event.name(), event.number());
    }
}
