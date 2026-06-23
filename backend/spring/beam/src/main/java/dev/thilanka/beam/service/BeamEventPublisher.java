package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.SectorEvent;
import dev.thilanka.beam.dto.SiteEvent;

public interface BeamEventPublisher {
    void publishSiteEvent(SiteEvent event);
    void publishSectorEvent(SectorEvent event);
}
