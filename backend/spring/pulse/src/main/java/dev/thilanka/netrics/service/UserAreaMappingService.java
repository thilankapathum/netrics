package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.AreaDto;
import dev.thilanka.netrics.dto.UserAreaMappingDto;
import dev.thilanka.netrics.entity.Area;

public interface UserAreaMappingService {
    UserAreaMappingDto createUserAreaMapping(UserAreaMappingDto userAreaMappingDto);
    Area findAreaByUserId(String userId);
    AreaDto getAreaByUserId(String userId);
}
