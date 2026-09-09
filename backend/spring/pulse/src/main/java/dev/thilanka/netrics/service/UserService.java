package dev.thilanka.netrics.service;

import dev.thilanka.netrics.dto.UserDto;
import dev.thilanka.netrics.entity.User;

import java.util.List;

public interface UserService {

    User createUser(User user);
    UserDto createUser(UserDto userDto);

    User findUserById(Long id);
    UserDto getUserById(Long id);

    User findUserByUsername(String username);

    User findByUserId(String userId);

    List<UserDto> getAllRealmUsers();
}
