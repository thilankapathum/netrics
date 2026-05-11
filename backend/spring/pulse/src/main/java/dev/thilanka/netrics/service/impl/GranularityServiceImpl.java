package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.GranularityDto;
import dev.thilanka.netrics.entity.Granularity;
import dev.thilanka.netrics.repository.GranularityRepository;
import dev.thilanka.netrics.service.GranularityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GranularityServiceImpl implements GranularityService {
    private final GranularityRepository granularityRepository;

    @Override
    public GranularityDto createGranularity(GranularityDto dto) {

        Granularity granularity = Granularity.builder()
                .name(dto.name())
                .label(dto.label())
                .plusSeconds(dto.plusSeconds())
                .build();

        Granularity savedGranularity = granularityRepository.save(granularity);

        return new GranularityDto(savedGranularity.getName(), savedGranularity.getLabel(), savedGranularity.getPlusSeconds());
    }

    @Override
    public List<GranularityDto> createGranularityList(List<GranularityDto> dtos) {
        List<GranularityDto> granularityDtos = new ArrayList<>();

        for (GranularityDto dto : dtos){
            GranularityDto savedGranularity = createGranularity(dto);
            granularityDtos.add(savedGranularity);
        }
        return granularityDtos;
    }

    @Override
    public Granularity findGranularityByName(String name) {
        return granularityRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("Granularity", "Name", name));
    }

    @Override
    public GranularityDto getGranularityByName(String name) {
        Granularity granularity = findGranularityByName(name);
        return new GranularityDto(granularity.getName(),granularity.getLabel(), granularity.getPlusSeconds());
    }

    @Override
    public Granularity findGranularityById(Long id) {
        return granularityRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Granularity", "ID", id));
    }

    @Override
    public GranularityDto getGranularityById(Long id) {
        Granularity granularity = findGranularityById(id);
        return new GranularityDto(granularity.getName(), granularity.getLabel(),granularity.getPlusSeconds());
    }

    @Override
    public List<Granularity> findAll() {
        return granularityRepository.findAll();
    }

    @Override
    public List<GranularityDto> getAll() {
        List<Granularity> granularities = findAll();

        return granularities.stream().map(g -> new GranularityDto(g.getName(),g.getLabel(),g.getPlusSeconds())).collect(Collectors.toList());
    }
}
