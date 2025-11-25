package dev.thilanka.netrics.service.impl;

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
    private final UserService userService;
    private final AreaService areaService;

    @Override
    public UserAreaMappingDto createUserAreaMapping(UserAreaMappingDto userAreaMappingDto) {

        User user = userService.findUserByUsername(userAreaMappingDto.username());
        Area area = areaService.findAreaByName(userAreaMappingDto.areaName());
        UserAreaMapping userAreaMapping = UserAreaMapping
                .builder()
                .area(area)
                .user(user)
                .build();

        UserAreaMapping savedUserAreaMapping = userAreaMappingRepository.save(userAreaMapping);

        return new UserAreaMappingDto(
                savedUserAreaMapping.getUser().getUsername(),
                savedUserAreaMapping.getArea().getName());
    }
}
