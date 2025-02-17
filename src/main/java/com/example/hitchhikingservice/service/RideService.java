package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.dto.RideDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RideService {
    private final List<RideDto> rides = List.of(
            new RideDto(1L, "Berlin", "Munich", "2025-02-20 10:00"),
            new RideDto(2L, "Hamburg", "Cologne", "2025-02-20 12:30"),
            new RideDto(3L, "Berlin", "Frankfurt", "2025-02-20 14:00")
    );

    public List<RideDto> getAllRides() {
        return rides;
    }

    public List<RideDto> getRidesByLocation(String departure, String destination) {
        return rides.stream()
                .filter(ride -> (departure == null || ride.getDeparture().equals(departure))
                        && (destination == null || ride.getDestination().equals(destination)))
                .toList();
    }

    public Optional<RideDto> getRideById(Long id) {
        return rides.stream()
                .filter(ride -> ride.getId().equals(id))
                .findFirst();
    }
}
