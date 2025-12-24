package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.User;
import dev.thilanka.netrics.entity.UserAreaMapping;
import dev.thilanka.netrics.repository.UserAreaMappingRepository;
import dev.thilanka.netrics.service.AreaService;
import dev.thilanka.netrics.service.UserAreaMappingService;
import dev.thilanka.netrics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAreaMappingServiceImpl implements UserAreaMappingService {
    private final UserAreaMappingRepository userAreaMappingRepository;
    private final AreaService areaService;

    @Override
    public UserAreaMappingDto createUserAreaMapping(UserAreaMappingDto userAreaMappingDto) {

        Area area = areaService.findAreaByName(userAreaMappingDto.areaName());
        UserAreaMapping userAreaMapping = UserAreaMapping
                .builder()
                .area(area)
                .userId(userAreaMappingDto.userId())
                .build();

        UserAreaMapping savedUserAreaMapping = userAreaMappingRepository.save(userAreaMapping);

        return new UserAreaMappingDto(
                savedUserAreaMapping.getUserId(),
                savedUserAreaMapping.getArea().getName());
    }

    @Override
    public Area findAreaByUserId(String userId) {
        UserAreaMapping userAreaMapping = userAreaMappingRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Area not found by User ID " + userId));
        return userAreaMapping.getArea();
    }

    @Override
    public AreaDto getAreaByUserId(String userId) {

        try {
            Area area = findAreaByUserId(userId);
            return new AreaDto(area.getName(),area.isEnabled(),area.getAreaType().getName());
        } catch (Exception e) {
            System.out.println("Error retrieving Area for User " + userId);
        }
        return null;
    }
}
