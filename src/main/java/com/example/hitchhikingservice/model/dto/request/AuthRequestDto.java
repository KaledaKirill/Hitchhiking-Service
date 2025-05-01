package com.example.hitchhikingservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDto(
        @NotBlank(message = "Name must not be blank")
        @Size(max = 127, message = "User name must not exceed 127 characters")
        String name,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email must not be blank")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone
) {}