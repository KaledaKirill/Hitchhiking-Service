package com.example.hitchhikingservice.service.impl;

import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import com.example.hitchhikingservice.model.entity.Ride;
import com.example.hitchhikingservice.model.entity.User;
import com.example.hitchhikingservice.repository.RideRepository;
import com.example.hitchhikingservice.repository.UserRepository;
import com.example.hitchhikingservice.service.RideService;
import com.example.hitchhikingservice.service.mapper.RideMapper;
import com.example.hitchhikingservice.utils.ErrorMessages;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final RideMapper rideMapper;

    @Override
    public List<RideResponseDto> getAllRides() {
        return rideRepository.findAll().stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    public RideResponseDto getRideById(Long id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.RIDE_NOT_FOUND));
        return rideMapper.toRideResponseDto(ride);
    }

    @Override
    public List<RideResponseDto> getRidesByDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));
        return rideRepository.findAllByDriver(driver).stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    public List<RideResponseDto> getRidesByPassenger(Long passengerId) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.PASSENGER_NOT_FOUND));
        return rideRepository.findAllByPassenger(passenger).stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public RideResponseDto createRide(RideRequestDto rideRequestDto) {
        User driver = userRepository.findById(rideRequestDto.driverId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.DRIVER_NOT_FOUND));
        Ride ride = rideMapper.toRide(rideRequestDto, driver);
        Ride savedRide = rideRepository.save(ride);
        return rideMapper.toRideResponseDto(savedRide);
    }

    @Override
    @Transactional
    public RideResponseDto updateRide(Long id, RideRequestDto rideRequestDto) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.RIDE_NOT_FOUND));

        ride.setCar(rideRequestDto.car());
        ride.setSeatsCount(rideRequestDto.seatsCount());
        ride.setDeparture(rideRequestDto.departure());
        ride.setDestination(rideRequestDto.destination());
        ride.setDepartureTime(rideRequestDto.departureTime());
        ride.setComment(rideRequestDto.comment());

        Ride updatedRide = rideRepository.save(ride);
        return rideMapper.toRideResponseDto(updatedRide);
    }

    @Override
    @Transactional
    public void deleteRideById(Long id) {
        if (!rideRepository.existsById(id)) {
            throw new EntityNotFoundException(ErrorMessages.RIDE_NOT_FOUND);
        }
        rideRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addPassengerToRide(Long rideId, Long userId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.RIDE_NOT_FOUND));
        User passenger = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));


        if (ride.getDriver().getId().equals(userId)) {
            throw new IllegalArgumentException("User is already a driver on this ride");
        }

        if (ride.getPassengers().contains(passenger)) {
            throw new IllegalArgumentException("User is already a passenger on this ride");
        }

        if (ride.getSeatsCount() <= 0) {
            throw new IllegalArgumentException("No available seats on this ride");
        }

        ride.getPassengers().add(passenger);
        int actualSeatsCount = ride.getSeatsCount() - 1;
        ride.setSeatsCount(actualSeatsCount);
        rideRepository.save(ride);
    }

    @Override
    @Transactional
    public void removePassengerFromRide(Long rideId, Long userId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.RIDE_NOT_FOUND));
        User passenger = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (!ride.getPassengers().contains(passenger)) {
            throw new IllegalArgumentException("User is not a passenger on this ride");
        }

        ride.getPassengers().remove(passenger);
        int actualSeatsCount = ride.getSeatsCount() + 1;
        ride.setSeatsCount(actualSeatsCount);
        rideRepository.save(ride);
    }
}
