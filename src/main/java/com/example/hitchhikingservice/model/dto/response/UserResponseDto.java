package com.example.hitchhikingservice.model.dto.response;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        String phone
) {}

