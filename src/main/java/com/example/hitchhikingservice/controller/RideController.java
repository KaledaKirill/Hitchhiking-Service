package com.example.hitchhikingservice.controller;

import com.example.hitchhikingservice.dto.RideDto;
import com.example.hitchhikingservice.service.RideService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {
    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping
    public List<RideDto> getAllRides() {
        return rideService.getAllRides();
    }

    @GetMapping("/search")
    public List<RideDto> searchRides(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination) {
        return rideService.getRidesByLocation(departure, destination);
    }

    @GetMapping("/{id}")
    public RideDto getRideById(@PathVariable Long id) {
        return rideService.getRideById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
    }
}
