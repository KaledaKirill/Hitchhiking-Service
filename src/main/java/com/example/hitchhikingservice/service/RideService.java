package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import java.util.List;

public interface RideService {

    List<RideResponseDto> getAllRides();

    RideResponseDto getRideById(Long id);

    List<RideResponseDto> getRidesByDriverId(Long driverId);

    List<RideResponseDto> getRidesByDriverName(String driverName);

    List<RideResponseDto> getRidesByPassengerId(Long passengerId);

    List<RideResponseDto> getRidesByPassengerName(String passengerName);

    RideResponseDto createRide(RideRequestDto rideRequestDto);

    List<RideResponseDto> createRides(List<RideRequestDto> rideRequestDtos);

    RideResponseDto updateRide(Long id, RideRequestDto rideRequestDto);

    void deleteRideById(Long id);

    void addPassengerToRide(Long rideId, Long userId);

    void removePassengerFromRide(Long rideId, Long userId);

}
