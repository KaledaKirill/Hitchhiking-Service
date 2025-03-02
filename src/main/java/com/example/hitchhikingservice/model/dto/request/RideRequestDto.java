package com.example.hitchhikingservice.model.dto.request;

import java.time.LocalDateTime;

public record RideRequestDto(
        String car,
        Integer seatsCount,
        String departure,
        String destination,
        LocalDateTime departureTime,
        String comment
) {}


