package com.example.hitchhikingservice.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RideResponseDto(
        Long id,
        String car,
        Integer seatsCount,
        String departure,
        String destination,
        LocalDateTime departureTime,
        String comment,
        Long driverId,
        List<Long> passengerIds
) {}

