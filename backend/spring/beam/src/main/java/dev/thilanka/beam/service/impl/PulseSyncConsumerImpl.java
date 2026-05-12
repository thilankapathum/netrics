package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.dto.BandEvent;
import dev.thilanka.beam.dto.SectorEvent;
import dev.thilanka.beam.dto.SiteEvent;
import dev.thilanka.beam.entity.*;
import dev.thilanka.beam.repository.BeamBandRepository;
import dev.thilanka.beam.repository.BeamSectorRepository;
import dev.thilanka.beam.repository.BeamSiteRepository;
import dev.thilanka.beam.service.PulseSyncConsumer;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class PulseSyncConsumerImpl implements PulseSyncConsumer {

    private final BeamSiteRepository beamSiteRepository;
    private final BeamSectorRepository beamSectorRepository;
    private final BeamBandRepository beamBandRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.site-events}",
            groupId = "beam-service",
            containerFactory = "siteEventListenerContainerFactory"
    )
    public void handleSiteEvent(SiteEvent event) {
        log.info("Received SiteEvent type={} id={} siteCode={}", event.eventType(), event.id(), event.siteCode());
        try {
            switch (event.eventType()) {
                case "CREATED", "UPDATED" -> upsertSite(event);
                case "DELETED" -> beamSiteRepository.deleteById(event.id());
                default -> log.warn("Unknown Site event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Error processing SiteEvent id={}", event.id(), e);
            throw e;
        }
    }

    @Override
    @KafkaListener(
            topics = "${spring.kafka.topic.sector-events}",
            groupId = "beam-service",
            containerFactory = "sectorEventListenerContainerFactory"
    )
    public void handleSectorEvent(SectorEvent event) {
        log.info("Received SectorEvent type={} id={} sectorName={}", event.eventType(), event.id(), event.name());
        try {
            switch (event.eventType()) {
                case "CREATED", "UPDATED" -> upsertSector(event);
                case "DELETED" -> beamSectorRepository.deleteById(event.id());
                default -> log.warn("Unknown Sector event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Error processing SectorEvent id={}", event.id(), e);
            throw e;
        }
    }

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

    private void upsertSite(SiteEvent event) {
        //-- Not using repository.save() to avoid duplicating with OperationLogListener PostPersist
        beamSiteRepository.upsert(
                event.id(), event.siteCode(), event.siteName(), event.latitude(), event.longitude(),
                event.createdAt(), event.createdBy()
        );
        log.info("Upserted site {} - {}", event.siteCode(), event.siteName());
    }

    private void upsertSector(SectorEvent event) {
        //TODO: Create Producer
        beamSectorRepository.upsert(event.id(), event.sectorIndex(), event.name(), event.azimuth(),
                event.siteId(), event.createdAt(), event.createdBy());
        log.info("Upserted sector {} - {}", event.sectorIndex(), event.name());
    }

    private void upsertBand(BandEvent event) {
        //TODO: Create Producer
        beamBandRepository.upsert(event.id(), event.name(), event.number(), event.unit(),
                event.createdAt(), event.createdBy());
        log.info("Upserted band {} - {}", event.name(), event.number());
    }
}
