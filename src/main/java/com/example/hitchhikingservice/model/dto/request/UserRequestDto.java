package com.example.hitchhikingservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        @NotBlank(message = "Name must not be blank")
        String name,

        @Email(message = "Invalid email format")
        String email,

        @Pattern(
                regexp = "^\\+(375|7)(\\d{9,10})$",
                message = "Phone number must be in format +375XXXXXXXXX or +7XXXXXXXXXX"
        )
        String phone
) {
}
