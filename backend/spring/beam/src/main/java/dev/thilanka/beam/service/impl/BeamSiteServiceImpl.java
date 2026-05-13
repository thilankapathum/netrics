package dev.thilanka.beam.service.impl;

import dev.thilanka.beam.dto.BeamSiteDto;
import dev.thilanka.beam.entity.BeamSite;
import dev.thilanka.beam.repository.BeamSiteRepository;
import dev.thilanka.beam.service.BeamSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BeamSiteServiceImpl implements BeamSiteService {
    private final BeamSiteRepository beamSiteRepository;

    @Override
    public BeamSite updateSite(BeamSite beamSite) {
        return null;
    }

    @Override
    public BeamSiteDto updateSite(BeamSiteDto dto) {
        return null;
    }
}
