package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.SectorEvent;
import dev.thilanka.netrics.dto.SiteEvent;
import dev.thilanka.netrics.repository.SectorRepository;
import dev.thilanka.netrics.repository.SiteRepository;
import dev.thilanka.netrics.service.BeamSyncConsumer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BeamSyncConsumerImpl implements BeamSyncConsumer {
    private final SiteRepository siteRepository;
    private final SectorRepository sectorRepository;


    @Override
    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.site-events}",
            groupId = "pulse-service",
            containerFactory = "siteEventListenerContainerFactory"
    )
    public void handleSiteEvent(SiteEvent event) {
        log.info("Received SiteEvent type={} siteCode={}", event.eventType(), event.siteCode());
        try {
            switch (event.eventType()) {
                case "CREATED", "UPDATED" -> upsertSite(event);
                case "DELETED" -> siteRepository.deleteBySiteCode(event.siteCode());
                default -> log.warn("Unknown Site event type: {}", event.eventType());
            }
            //TODO: RELOAD SITES TO MEMORY  | EVICT CACHE
        } catch (Exception e) {
            log.error("Error processing SiteEvent siteCode={}", event.siteCode(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    @KafkaListener(
            topics = "${spring.kafka.topic.sector-events}",
            groupId = "pulse-service",
            containerFactory = "sectorEventListenerContainerFactory"
    )
    public void handleSectorEvent(SectorEvent event) {
        log.info("Received SectorEvent type={} sectorName={} sectorIndex={}", event.eventType(), event.name(), event.sectorIndex());
        try {
            switch (event.eventType()) {
                case "CREATED", "UPDATED" -> upsertSector(event);
                case "DELETED" -> sectorRepository.deleteByName(event.name());
                default -> log.warn("Unknown Sector event type: {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Error processing SectorEvent name={}", event.name(), e);
            throw e;
        }
    }

    private void upsertSite(SiteEvent event) {
        //-- Not using repository.save() to avoid duplicating with OperationLogListener PostPersist
        siteRepository.upsert(
                event.siteCode(), event.siteName(), event.latitude(), event.longitude(),
                event.createdAt(), event.modifiedAt(),event.createdBy(), event.modifiedBy()
        );
        log.info("Upserted site {} - {}", event.siteCode(), event.siteName());
    }

    private void upsertSector(SectorEvent event) {
        //TODO: Create Producer
        sectorRepository.upsert(event.sectorIndex(), event.name(), event.azimuth(),
                event.siteId(), event.createdAt(), event.modifiedAt(), event.createdBy(), event.modifiedBy());
        log.info("Upserted sector {} - {}", event.sectorIndex(), event.name());
    }
}
