package com.example.hitchhikingservice.model.dto.request;

public record UserRequestDto(
        String name,
        String email,
        String phone
) {
}

