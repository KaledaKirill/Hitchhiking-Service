package com.example.hitchhikingservice.controller;

import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import com.example.hitchhikingservice.security.JwtService;
import com.example.hitchhikingservice.service.RideService;
import com.example.hitchhikingservice.service.mapper.RideMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
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
@Tag(name = "Ride API", description = "Operations on rides")
public class RideController {

    private final RideService rideService;
    private final JwtService jwtService;

    @GetMapping
    @Operation(summary = "Get all rides with optional filtering")
    public ResponseEntity<List<RideResponseDto>> getAllRides(
            @RequestParam(required = false) String departure,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) String date
    ) {
        return ResponseEntity.ok(rideService.getAllRides(departure, destination, date));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ride by ID")
    public ResponseEntity<RideResponseDto> getRideById(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @GetMapping("/search/driver/id")
    @Operation(summary = "Get ride by driver ID")
    public ResponseEntity<List<RideResponseDto>> getRidesByDriverId(
            @RequestParam Long driverId
    ) {
        return ResponseEntity.ok(rideService.getRidesByDriverId(driverId));
    }

    @GetMapping("/search/driver/name")
    @Operation(summary = "Get ride by driver username")
    public ResponseEntity<List<RideResponseDto>> getRidesByDriverName(
            @RequestParam String driverName
    ) {
        return ResponseEntity.ok(rideService.getRidesByDriverName(driverName));
    }

    @GetMapping("/search/passenger/id")
    @Operation(summary = "Get ride by passenger ID")
    public ResponseEntity<List<RideResponseDto>> getRidesByPassengerId(
            @RequestParam Long passengerId
    ) {
        return ResponseEntity.ok(rideService.getRidesByPassengerId(passengerId));
    }

    @GetMapping("/search/passenger/name")
    @Operation(summary = "Get ride by passenger username")
    public ResponseEntity<List<RideResponseDto>> getRidesByPassengerName(
            @RequestParam String passengerName
    ) {
        return ResponseEntity.ok(rideService.getRidesByPassengerName(passengerName));
    }

    @PostMapping("/create")
    @Operation(summary = "Create ride")
    public ResponseEntity<RideResponseDto> createRide(
            @Valid @RequestBody RideRequestDto rideRequestDto,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new RuntimeException("Authentication or token is missing");
        }
        String jwtToken = authentication.getCredentials().toString();
        Long driverId = jwtService.getUserIdFromToken(jwtToken);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.createRide(rideRequestDto, driverId));
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update ride")
    public ResponseEntity<RideResponseDto> updateRide(
            @PathVariable Long id,
            @Valid @RequestBody RideRequestDto rideRequestDto,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new RuntimeException("Authentication or token is missing");
        }
        String jwtToken = authentication.getCredentials().toString();
        Long userId = jwtService.getUserIdFromToken(jwtToken);
        return ResponseEntity.ok(rideService.updateRide(id, rideRequestDto, userId));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete ride")
    public ResponseEntity<Void> deleteRide(
            @PathVariable Long id,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new RuntimeException("Authentication or token is missing");
        }
        String jwtToken = authentication.getCredentials().toString();
        Long userId = jwtService.getUserIdFromToken(jwtToken);
        rideService.deleteRideById(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{rideId}/passenger/{userId}")
    @Operation(summary = "Add passenger to ride")
    public ResponseEntity<Void> addPassengerToRide(
            @PathVariable Long rideId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new RuntimeException("Authentication or token is missing");
        }
        String jwtToken = authentication.getCredentials().toString();
        Long currentUserId = jwtService.getUserIdFromToken(jwtToken);
        if (!currentUserId.equals(userId)) {
            throw new RuntimeException("You can only join yourself as a passenger");
        }
        rideService.addPassengerToRide(rideId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{rideId}/passenger/{userId}")
    @Operation(summary = "Remove passenger from ride")
    public ResponseEntity<Void> removePassengerFromRide(
            @PathVariable Long rideId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getCredentials() == null) {
            throw new RuntimeException("Authentication or token is missing");
        }
        String jwtToken = authentication.getCredentials().toString();
        Long currentUserId = jwtService.getUserIdFromToken(jwtToken);
        if (!currentUserId.equals(userId)) {
            throw new RuntimeException("You can only leave yourself from a ride");
        }
        rideService.removePassengerFromRide(rideId, userId);
        return ResponseEntity.noContent().build();
    }
}
