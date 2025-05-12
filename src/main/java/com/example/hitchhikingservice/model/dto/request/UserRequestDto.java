package com.example.hitchhikingservice.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @NotBlank(message = "Name must not be blank")
        @Size(max = 127, message = "User username not exceed 127 characters")
        String name,

        @NotBlank(message = "Phone must not be blank")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone
) {
}
