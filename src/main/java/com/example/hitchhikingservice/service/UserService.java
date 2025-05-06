package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.model.dto.request.UserRequestDto;
import com.example.hitchhikingservice.model.dto.response.UserResponseDto;
import java.util.List;

public interface UserService {

    List<UserResponseDto> getAllUsers();

    UserResponseDto getUserById(Long id);

    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto updateUser(Long id, UserRequestDto userRequestDto);

    void deleteUserById(Long id);

    UserResponseDto getUserByEmail(String userEmail);
}
