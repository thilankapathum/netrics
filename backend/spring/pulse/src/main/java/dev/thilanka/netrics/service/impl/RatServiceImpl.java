package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.RatDto;
import dev.thilanka.netrics.entity.Rat;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.RatRepository;
import dev.thilanka.netrics.service.RatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatServiceImpl implements RatService {
    private final RatRepository ratRepository;
    private final Mapper mapper;

    @Override
    public List<RatDto> getAll() {
        List<Rat> rats = ratRepository.findAll();

        return rats.stream().map(mapper::toRatDto).collect(Collectors.toList());
    }

    @Override
    public List<Rat> findAll() {
        return ratRepository.findAll();
    }

    @Override
    public RatDto getRatDtoByName(String name) {
        Rat rat = ratRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("RAT", "Name", name));

        return mapper.toRatDto(rat);
    }

    @Override
    public RatDto getRatDtoByLabel(String label) {
        Rat rat = ratRepository.findByLabel(label)
                .orElseThrow(() -> new ResourceNotFoundException("RAT", "Label", label));
        return mapper.toRatDto(rat);
    }

    @Override
    public RatDto createRat(RatDto dto) {
        Rat rat = mapper.ratDtoToRat(dto);
        Rat savedRat = ratRepository.save(rat);
        return mapper.toRatDto(savedRat);
    }

    @Override
    public Rat findRatByName(String name) {
        return ratRepository.findByName(name)
                .orElseThrow(()-> new ResourceNotFoundException("RAT", "Name", name));
    }

    @Override
    public List<RatDto> createRatList(List<RatDto> ratDtos) {
        List<RatDto> savedRats = new ArrayList<>();

        for (RatDto dto: ratDtos){
            savedRats.add(createRat(dto));
        }
        return savedRats;
    }
}
