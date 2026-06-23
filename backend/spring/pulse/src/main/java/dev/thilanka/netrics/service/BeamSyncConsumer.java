package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.BandEvent;
import dev.thilanka.netrics.dto.SectorEvent;
import dev.thilanka.netrics.dto.SiteEvent;

public interface BeamSyncConsumer {
    void handleSiteEvent(SiteEvent event);
    void handleSectorEvent(SectorEvent event);
}
