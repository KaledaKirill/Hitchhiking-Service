package com.example.hitchhikingservice.service.impl;

import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.model.dto.request.UserRequestDto;
import com.example.hitchhikingservice.model.dto.response.UserResponseDto;
import com.example.hitchhikingservice.model.entity.User;
import com.example.hitchhikingservice.repository.UserRepository;
import com.example.hitchhikingservice.service.UserService;
import com.example.hitchhikingservice.service.mapper.UserMapper;
import com.example.hitchhikingservice.utils.ErrorMessages;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));
        return userMapper.toUserResponseDto(user);
    }

    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = userMapper.toUser(userRequestDto);
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(savedUser);
    }

    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));

        user.setName(userRequestDto.name());
        user.setEmail(userRequestDto.email());
        user.setPhone(userRequestDto.phone());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponseDto(updatedUser);
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }
}