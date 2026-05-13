package dev.thilanka.beam.service;

import dev.thilanka.beam.dto.BeamSiteDto;
import dev.thilanka.beam.entity.BeamSite;

public interface BeamSiteService {

    BeamSite updateSite(BeamSite beamSite);

    BeamSiteDto updateSite(BeamSiteDto dto);
}
