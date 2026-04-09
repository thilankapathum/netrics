package dev.thilanka.netrics.service.impl;

import dev.thilanka.netrics.common.exception.ResourceNotFoundException;
import dev.thilanka.netrics.dto.UserDto;
import dev.thilanka.netrics.entity.User;
import dev.thilanka.netrics.mapper.Mapper;
import dev.thilanka.netrics.repository.UserRepository;
import dev.thilanka.netrics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;


    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = User.builder()
                .userId(userDto.userId())
                .firstName(userDto.firstName())
                .lastName(userDto.lastName())
                .username(userDto.username())
                .email(userDto.email())
                .build();
        User savedUser = createUser(user);
        return mapper.userToDto(savedUser);
    }

    @Override
    public User findUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User", "ID", id));
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = findUserById(id);
        return mapper.userToDto(user);
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(()-> new ResourceNotFoundException("User", "Username", username));
    }

    @Override
    public User findByUserId(String userId) {
        return userRepository
                .findByUserId(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User", "User ID", userId));
    }
}
