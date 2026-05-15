package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.BandEvent;
import dev.thilanka.netrics.dto.SectorEvent;
import dev.thilanka.netrics.dto.SiteEvent;

public interface PulseEventPublisher {
//    void publishSiteEvent(SiteEvent event);
//    void publishSectorEvent(SectorEvent event);
    void publishBandEvent(BandEvent event);
}
