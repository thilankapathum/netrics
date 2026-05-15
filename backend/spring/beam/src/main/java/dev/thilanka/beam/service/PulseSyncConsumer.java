package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.BandEvent;
import dev.thilanka.beam.dto.SectorEvent;
import dev.thilanka.beam.dto.SiteEvent;

public interface PulseSyncConsumer {
//    void handleSiteEvent(SiteEvent event);

//    void handleSectorEvent(SectorEvent event);

    void handleBandEvent(BandEvent event);
}
