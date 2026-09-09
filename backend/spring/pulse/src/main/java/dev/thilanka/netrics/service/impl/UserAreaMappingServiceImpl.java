package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.dto.UserAreaMappingUpdateDto;
import dev.thilanka.netrics.dto.UserDto;
import dev.thilanka.netrics.entity.Area;
import dev.thilanka.netrics.entity.UserAreaMapping;
import dev.thilanka.netrics.repository.UserAreaMappingRepository;
import dev.thilanka.netrics.service.AreaService;
import dev.thilanka.netrics.service.UserAreaMappingService;
import dev.thilanka.netrics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class UserAreaMappingServiceImpl implements UserAreaMappingService {
    private final UserAreaMappingRepository userAreaMappingRepository;
    private final AreaService areaService;
    private final UserService userService;

    @Override
    public UserAreaMappingDto createUserAreaMapping(UserAreaMappingDto userAreaMappingDto) {

        Area area = areaService.findAreaByName(userAreaMappingDto.areaName());
        UserAreaMapping userAreaMapping = UserAreaMapping
                .builder()
                .area(area)
                .userId(userAreaMappingDto.userId())
                .build();

        UserAreaMapping savedUserAreaMapping = userAreaMappingRepository.save(userAreaMapping);

        return toDto(savedUserAreaMapping, userService.getRealmUserById(savedUserAreaMapping.getUserId()));
    }

    @Override
    public Area findAreaByUserId(String userId) {
        UserAreaMapping userAreaMapping = userAreaMappingRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Area", "User ID", userId));
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

    @Override
    public List<UserAreaMappingDto> getAllUserAreaMappings() {
        Map<String, UserDto> usersById = userService.getAllRealmUsers().stream()
                .collect(java.util.stream.Collectors.toMap(UserDto::userId, Function.identity()));

        return userAreaMappingRepository.findAll().stream()
                .map(mapping -> toDto(mapping, usersById.get(mapping.getUserId())))
                .toList();
    }

    @Override
    public UserAreaMappingDto updateUserAreaMapping(Long id, UserAreaMappingUpdateDto updateDto) {
        UserAreaMapping userAreaMapping = userAreaMappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserAreaMapping", "ID", id));

        Area area = areaService.findAreaByName(updateDto.areaName());
        userAreaMapping.setArea(area);
        UserAreaMapping savedUserAreaMapping = userAreaMappingRepository.save(userAreaMapping);

        return toDto(savedUserAreaMapping, userService.getRealmUserById(savedUserAreaMapping.getUserId()));
    }

    @Override
    public void deleteUserAreaMapping(Long id) {
        if (!userAreaMappingRepository.existsById(id)) {
            throw new ResourceNotFoundException("UserAreaMapping", "ID", id);
        }
        userAreaMappingRepository.deleteById(id);
    }

    private UserAreaMappingDto toDto(UserAreaMapping mapping, UserDto user) {
        String userFullName = user != null ? (user.firstName() + " " + user.lastName()) : mapping.getUserId();
        return new UserAreaMappingDto(
                mapping.getId(),
                mapping.getUserId(),
                userFullName,
                mapping.getArea().getAreaType().getName(),
                mapping.getArea().getName());
    }
}
