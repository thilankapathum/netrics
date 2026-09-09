package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.dto.UserAreaMappingUpdateDto;
import dev.thilanka.netrics.entity.Area;

import java.util.List;

public interface UserAreaMappingService {
    UserAreaMappingDto createUserAreaMapping(UserAreaMappingDto userAreaMappingDto);
    Area findAreaByUserId(String userId);
    AreaDto getAreaByUserId(String userId);
    List<UserAreaMappingDto> getAllUserAreaMappings();
    UserAreaMappingDto updateUserAreaMapping(Long id, UserAreaMappingUpdateDto updateDto);
    void deleteUserAreaMapping(Long id);
}
