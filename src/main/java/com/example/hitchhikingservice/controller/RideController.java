package com.example.hitchhikingservice.controller;

import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import com.example.hitchhikingservice.service.RideService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @GetMapping
    public ResponseEntity<List<RideResponseDto>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponseDto> getRideById(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @GetMapping("/driver")
    public ResponseEntity<List<RideResponseDto>> getRidesByDriverId(
            @RequestParam Long driverId
    ) {
        return ResponseEntity.ok(rideService.getRidesByDriver(driverId));
    }

    @GetMapping("/passenger")
    public ResponseEntity<List<RideResponseDto>> getRidesByPassengerId(
            @RequestParam Long passengerId
    ) {
        return ResponseEntity.ok(rideService.getRidesByPassenger(passengerId));
    }

    @PostMapping("/create")
    public ResponseEntity<RideResponseDto> createRide(
            @RequestBody RideRequestDto rideRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.createRide(rideRequestDto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RideResponseDto> updateRide(
            @PathVariable Long id,
            @RequestBody RideRequestDto rideRequestDto) {
        return ResponseEntity.ok(rideService.updateRide(id, rideRequestDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRide(@PathVariable Long id) {
        rideService.deleteRideById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/passenger/{userId}")
    public ResponseEntity<Void> addPassengerToRide(
            @PathVariable Long rideId,
            @PathVariable Long userId) {
        rideService.addPassengerToRide(rideId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{rideId}/passenger/{userId}")
    public ResponseEntity<Void> removePassengerFromRide(
            @PathVariable Long rideId,
            @PathVariable Long userId) {
        rideService.removePassengerFromRide(rideId, userId);
        return ResponseEntity.noContent().build();
    }
}
