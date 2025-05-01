package com.example.hitchhikingservice.model.dto.response;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        String phone
) {}

