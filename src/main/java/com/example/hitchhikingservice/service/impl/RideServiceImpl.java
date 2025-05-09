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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final RideMapper rideMapper;

    public List<RideResponseDto> getAllRides(String departure, String destination, String date) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (date != null && !date.isEmpty()) {
            LocalDate localDate = LocalDate.parse(date);
            startDate = localDate.atStartOfDay();
            endDate = localDate.atTime(LocalTime.MAX);
        }

        List<Ride> rides = rideRepository.findAll();

        final LocalDateTime finalStartDate = startDate;
        final LocalDateTime finalEndDate = endDate;

        List<Ride> filteredRides = rides.stream()
                .filter(ride -> departure == null || departure.isEmpty() || ride.getDeparture().toLowerCase().contains(departure.toLowerCase()))
                .filter(ride -> destination == null || destination.isEmpty() || ride.getDestination().toLowerCase().contains(destination.toLowerCase()))
                .filter(ride -> finalStartDate == null || !ride.getDepartureTime().isBefore(finalStartDate))
                .filter(ride -> finalEndDate == null || !ride.getDepartureTime().isAfter(finalEndDate))
                .toList();

        return filteredRides.stream()
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
    public List<RideResponseDto> getRidesByDriverId(Long driverId) {
        if (!userRepository.existsById(driverId)) {
            throw new EntityNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }
        return rideRepository.findAllByDriverId(driverId).stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    public List<RideResponseDto> getRidesByDriverName(String driverName) {
        if (!userRepository.existsByName(driverName)) {
            throw new EntityNotFoundException(ErrorMessages.DRIVER_NOT_FOUND);
        }
        return rideRepository.findAllByDriverName(driverName).stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    public List<RideResponseDto> getRidesByPassengerId(Long passengerId) {
        if (!userRepository.existsById(passengerId)) {
            throw new EntityNotFoundException(ErrorMessages.PASSENGER_NOT_FOUND);
        }
        return rideRepository.findAllByPassengerId(passengerId).stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    public List<RideResponseDto> getRidesByPassengerName(String passengerName) {
        if (!userRepository.existsByName(passengerName)) {
            throw new EntityNotFoundException(ErrorMessages.PASSENGER_NOT_FOUND);
        }
        return rideRepository.findAllByPassengerName(passengerName).stream()
                .map(rideMapper::toRideResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public RideResponseDto createRide(RideRequestDto rideRequestDto, Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + driverId));
        Ride ride = rideMapper.toRide(rideRequestDto, driver);

        Ride savedRide = rideRepository.save(ride);
        return rideMapper.toRideResponseDto(savedRide);
    }

    @Override
    @Transactional
    public RideResponseDto updateRide(Long id, RideRequestDto rideRequestDto, Long userId) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found with id: " + id));
        if (!ride.getDriver().getId().equals(userId)) {
            throw new RuntimeException("Only the driver can update this ride");
        }

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
    public void deleteRideById(Long id, Long userId) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ride not found with id: " + id));
        if (!ride.getDriver().getId().equals(userId)) {
            throw new RuntimeException("Only the driver can delete this ride");
        }
        rideRepository.delete(ride);
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