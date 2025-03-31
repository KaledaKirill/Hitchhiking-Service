package com.example.hitchhikingservice.model.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record RideRequestDto(
        @NotNull(message = "Driver ID must not be null")
        Long driverId,

        @NotBlank(message = "Car must not be blank")
        String car,

        @NotNull(message = "Seats count must not be null")
        @Positive(message = "Seats count must be a positive number")
        Integer seatsCount,

        @NotBlank(message = "Departure must not be blank")
        @Size(max = 255, message = "Departure location must not exceed 255 characters")
        String departure,

        @NotBlank(message = "Destination must not be blank")
        @Size(max = 255, message = "Destination location must not exceed 255 characters")
        String destination,

        @NotNull(message = "Departure time must not be null")
        @Future(message = "Departure time must be in the future")
        LocalDateTime departureTime,

        @Size(max = 500, message = "Comment must not exceed 500 characters")
        String comment
) {}
