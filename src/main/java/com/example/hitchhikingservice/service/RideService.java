package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import java.util.List;

public interface RideService {

    List<RideResponseDto> getAllRides();

    RideResponseDto getRideById(Long id);

    List<RideResponseDto> getRidesByDriver(Long driverId);

    List<RideResponseDto> getRidesByPassenger(Long passengerId);

    RideResponseDto createRide(RideRequestDto rideRequestDto, Long driverId);

    RideResponseDto updateRide(Long id, RideRequestDto rideRequestDto);

    void deleteRideById(Long id);

    void addPassengerToRide(Long rideId, Long userId);

    void removePassengerFromRide(Long rideId, Long userId);
}
