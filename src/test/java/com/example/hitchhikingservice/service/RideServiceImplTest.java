package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.cache.RideCache;
import com.example.hitchhikingservice.exception.EntityNotFoundException;
import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import com.example.hitchhikingservice.model.dto.response.UserResponseDto;
import com.example.hitchhikingservice.model.entity.Ride;
import com.example.hitchhikingservice.model.entity.User;
import com.example.hitchhikingservice.repository.RideRepository;
import com.example.hitchhikingservice.repository.UserRepository;
import com.example.hitchhikingservice.service.impl.RideServiceImpl;
import com.example.hitchhikingservice.service.mapper.RideMapper;
import com.example.hitchhikingservice.utils.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {
    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RideMapper rideMapper;

    @Mock
    private RideCache rideCache;

    @InjectMocks
    private RideServiceImpl rideService;

    private final UserResponseDto driverDto = new UserResponseDto(1L, "John", "john@example.com", "123456789");

    @Test
    void getAllRides_shouldReturnAllRides() {
        Ride ride1 = new Ride();
        ride1.setId(1L);
        Ride ride2 = new Ride();
        ride2.setId(2L);
        List<Ride> rides = List.of(ride1, ride2);

        RideResponseDto dto1 = new RideResponseDto(1L, "Car1", 2, "A", "B", LocalDateTime.now(), "Comment1", driverDto, List.of());
        RideResponseDto dto2 = new RideResponseDto(2L, "Car2", 3, "X", "Y", LocalDateTime.now(), "Comment2", driverDto, List.of());
        List<RideResponseDto> expectedDtos = List.of(dto1, dto2);

        when(rideRepository.findAll()).thenReturn(rides);
        when(rideMapper.toRideResponseDto(ride1)).thenReturn(dto1);
        when(rideMapper.toRideResponseDto(ride2)).thenReturn(dto2);

        List<RideResponseDto> result = rideService.getAllRides();

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    void getRideById_shouldReturnRideFromCache_whenRideExistsInCache() {
        Ride ride = new Ride();
        RideResponseDto responseDto = new RideResponseDto(1L, "Car", 2, "A", "B",
                LocalDateTime.now(), "Comment", driverDto, List.of());

        when(rideCache.get(1L)).thenReturn(ride);
        when(rideMapper.toRideResponseDto(ride)).thenReturn(responseDto);

        RideResponseDto result = rideService.getRideById(1L);

        assertThat(result).isEqualTo(responseDto);
        verify(rideRepository, never()).findById(anyLong());
    }

    @Test
    void getRideById_shouldReturnRideFromDatabaseAndCacheIt_whenRideNotInCache() {
        Ride ride = new Ride();
        ride.setId(1L);
        RideResponseDto dto = new RideResponseDto(1L, "Car", 2, "A", "B",
                LocalDateTime.now(), "Comment", driverDto, List.of());

        when(rideCache.get(1L)).thenReturn(null);
        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideMapper.toRideResponseDto(ride)).thenReturn(dto);

        RideResponseDto result = rideService.getRideById(1L);

        assertThat(result).isEqualTo(dto);
        verify(rideCache).put(1L, ride);
    }

    @Test
    void getRideById_shouldThrowException_whenRideNotFound() {
        when(rideCache.get(99L)).thenReturn(null);
        when(rideRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.getRideById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.RIDE_NOT_FOUND);
    }

    @Test
    void getRidesByDriverId_shouldReturnRides_whenDriverExists() {
        Long driverId = 1L;
        Ride ride1 = new Ride();
        ride1.setId(1L);
        Ride ride2 = new Ride();
        ride2.setId(2L);
        List<Ride> rides = List.of(ride1, ride2);

        RideResponseDto dto1 = new RideResponseDto(1L, "Car1", 2, "A", "B", LocalDateTime.now(), "Comment1", driverDto, List.of());
        RideResponseDto dto2 = new RideResponseDto(2L, "Car2", 3, "X", "Y", LocalDateTime.now(), "Comment2", driverDto, List.of());
        List<RideResponseDto> expectedDtos = List.of(dto1, dto2);

        when(userRepository.existsById(driverId)).thenReturn(true);
        when(rideRepository.findAllByDriverId(driverId)).thenReturn(rides);
        when(rideMapper.toRideResponseDto(ride1)).thenReturn(dto1);
        when(rideMapper.toRideResponseDto(ride2)).thenReturn(dto2);

        List<RideResponseDto> result = rideService.getRidesByDriverId(driverId);

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    void getRidesByDriverId_shouldThrowException_whenDriverNotFound() {
        Long driverId = 99L;
        when(userRepository.existsById(driverId)).thenReturn(false);

        assertThatThrownBy(() -> rideService.getRidesByDriverId(driverId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.DRIVER_NOT_FOUND);
    }

    @Test
    void getRidesByDriverName_shouldReturnRides_whenDriverExists() {
        String driverName = "John";
        Ride ride1 = new Ride();
        ride1.setId(1L);
        Ride ride2 = new Ride();
        ride2.setId(2L);
        List<Ride> rides = List.of(ride1, ride2);

        RideResponseDto dto1 = new RideResponseDto(1L, "Car1", 2, "A", "B", LocalDateTime.now(), "Comment1", driverDto, List.of());
        RideResponseDto dto2 = new RideResponseDto(2L, "Car2", 3, "X", "Y", LocalDateTime.now(), "Comment2", driverDto, List.of());
        List<RideResponseDto> expectedDtos = List.of(dto1, dto2);

        when(userRepository.existsByName(driverName)).thenReturn(true);
        when(rideRepository.findAllByDriverName(driverName)).thenReturn(rides);
        when(rideMapper.toRideResponseDto(ride1)).thenReturn(dto1);
        when(rideMapper.toRideResponseDto(ride2)).thenReturn(dto2);

        List<RideResponseDto> result = rideService.getRidesByDriverName(driverName);

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    void getRidesByDriverName_shouldThrowException_whenDriverNotFound() {
        String driverName = "Unknown";
        when(userRepository.existsByName(driverName)).thenReturn(false);

        assertThatThrownBy(() -> rideService.getRidesByDriverName(driverName))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.DRIVER_NOT_FOUND);
    }

    @Test
    void getRidesByPassengerId_shouldReturnRides_whenPassengerExists() {
        Long passengerId = 2L;
        Ride ride1 = new Ride();
        ride1.setId(1L);
        Ride ride2 = new Ride();
        ride2.setId(2L);
        List<Ride> rides = List.of(ride1, ride2);

        RideResponseDto dto1 = new RideResponseDto(1L, "Car1", 2, "A", "B", LocalDateTime.now(), "Comment1", driverDto, List.of());
        RideResponseDto dto2 = new RideResponseDto(2L, "Car2", 3, "X", "Y", LocalDateTime.now(), "Comment2", driverDto, List.of());
        List<RideResponseDto> expectedDtos = List.of(dto1, dto2);

        when(userRepository.existsById(passengerId)).thenReturn(true);
        when(rideRepository.findAllByPassengerId(passengerId)).thenReturn(rides);
        when(rideMapper.toRideResponseDto(ride1)).thenReturn(dto1);
        when(rideMapper.toRideResponseDto(ride2)).thenReturn(dto2);

        List<RideResponseDto> result = rideService.getRidesByPassengerId(passengerId);

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    void getRidesByPassengerId_shouldThrowException_whenPassengerNotFound() {
        Long passengerId = 99L;
        when(userRepository.existsById(passengerId)).thenReturn(false);

        assertThatThrownBy(() -> rideService.getRidesByPassengerId(passengerId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.PASSENGER_NOT_FOUND);
    }

    @Test
    void getRidesByPassengerName_shouldReturnRides_whenPassengerExists() {
        String passengerName = "Alice";
        Ride ride1 = new Ride();
        ride1.setId(1L);
        Ride ride2 = new Ride();
        ride2.setId(2L);
        List<Ride> rides = List.of(ride1, ride2);

        RideResponseDto dto1 = new RideResponseDto(1L, "Car1", 2, "A", "B", LocalDateTime.now(), "Comment1", driverDto, List.of());
        RideResponseDto dto2 = new RideResponseDto(2L, "Car2", 3, "X", "Y", LocalDateTime.now(), "Comment2", driverDto, List.of());
        List<RideResponseDto> expectedDtos = List.of(dto1, dto2);

        when(userRepository.existsByName(passengerName)).thenReturn(true);
        when(rideRepository.findAllByPassengerName(passengerName)).thenReturn(rides);
        when(rideMapper.toRideResponseDto(ride1)).thenReturn(dto1);
        when(rideMapper.toRideResponseDto(ride2)).thenReturn(dto2);

        List<RideResponseDto> result = rideService.getRidesByPassengerName(passengerName);

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    void getRidesByPassengerName_shouldThrowException_whenPassengerNotFound() {
        String passengerName = "Unknown";
        when(userRepository.existsByName(passengerName)).thenReturn(false);

        assertThatThrownBy(() -> rideService.getRidesByPassengerName(passengerName))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.PASSENGER_NOT_FOUND);
    }

    @Test
    void createRide_shouldSaveAndReturnNewRide_whenDriverExists() {
        RideRequestDto request = new RideRequestDto(1L, "Car", 2, "A", "B", LocalDateTime.now(), "Comment");
        User driver = new User();
        Ride ride = new Ride();
        Ride savedRide = new Ride();
        savedRide.setId(1L);
        RideResponseDto dto = new RideResponseDto(1L, "Car", 2, "A", "B",
                LocalDateTime.now(), "Comment", driverDto, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(rideMapper.toRide(request, driver)).thenReturn(ride);
        when(rideRepository.save(ride)).thenReturn(savedRide);
        when(rideMapper.toRideResponseDto(savedRide)).thenReturn(dto);

        RideResponseDto result = rideService.createRide(request);

        assertThat(result).isEqualTo(dto);
        verify(rideCache).put(savedRide.getId(), savedRide);
    }

    @Test
    void createRide_shouldThrowException_whenDriverNotFound() {
        RideRequestDto request = new RideRequestDto(99L, "Car", 2, "A", "B", LocalDateTime.now(), "Comment");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.createRide(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.DRIVER_NOT_FOUND);
    }

    @Test
    void updateRide_shouldUpdateAndReturnRide_whenRideExists() {
        User driver = new User();
        driver.setId(1L);

        Ride existingRide = new Ride();
        existingRide.setId(1L);
        existingRide.setDriver(driver);
        existingRide.setCar("Car");
        existingRide.setSeatsCount(2);
        existingRide.setDeparture("A");
        existingRide.setDestination("B");
        existingRide.setDepartureTime(LocalDateTime.now());
        existingRide.setComment("Comment");
        existingRide.setPassengers(new ArrayList<>());

        RideRequestDto requestDto = new RideRequestDto(1L, "UpdatedCar", 3, "X", "Y",
                LocalDateTime.now().plusHours(1), "UpdatedComment");

        RideResponseDto responseDto = new RideResponseDto(1L, "UpdatedCar", 3, "X", "Y",
                LocalDateTime.now().plusHours(1), "UpdatedComment", driverDto, List.of());

        when(rideRepository.findById(1L)).thenReturn(Optional.of(existingRide));
        when(rideRepository.save(existingRide)).thenReturn(existingRide);
        when(rideMapper.toRideResponseDto(existingRide)).thenReturn(responseDto);

        RideResponseDto result = rideService.updateRide(1L, requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(existingRide.getCar()).isEqualTo("UpdatedCar");
        assertThat(existingRide.getSeatsCount()).isEqualTo(3);
        assertThat(existingRide.getDeparture()).isEqualTo("X");
        assertThat(existingRide.getDestination()).isEqualTo("Y");
        assertThat(existingRide.getDepartureTime()).isEqualTo(requestDto.departureTime());
        assertThat(existingRide.getComment()).isEqualTo("UpdatedComment");

        verify(rideRepository).save(existingRide);
        verify(rideCache).put(1L, existingRide);
    }

    @Test
    void updateRide_shouldThrowException_whenRideNotFound() {
        RideRequestDto requestDto = new RideRequestDto(1L, "UpdatedCar", 3, "X", "Y",
                LocalDateTime.now().plusHours(1), "UpdatedComment");

        when(rideRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rideService.updateRide(99L, requestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.RIDE_NOT_FOUND);
    }

    @Test
    void deleteRideById_shouldDeleteRideAndRemoveFromCache_whenRideExists() {
        when(rideRepository.existsById(1L)).thenReturn(true);

        rideService.deleteRideById(1L);

        verify(rideRepository).deleteById(1L);
        verify(rideCache).remove(1L);
    }

    @Test
    void deleteRideById_shouldThrowException_whenRideNotFound() {
        when(rideRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> rideService.deleteRideById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(ErrorMessages.RIDE_NOT_FOUND);
    }

    @Test
    void addPassengerToRide_shouldAddPassenger_whenUserIsValid() {
        Ride ride = new Ride();
        ride.setSeatsCount(1);
        ride.setPassengers(new ArrayList<>());
        User driver = new User();
        driver.setId(1L);
        User passenger = new User();
        passenger.setId(2L);
        ride.setDriver(driver);

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(userRepository.findById(2L)).thenReturn(Optional.of(passenger));
        when(rideRepository.save(ride)).thenReturn(ride);

        rideService.addPassengerToRide(1L, 2L);

        assertThat(ride.getPassengers()).contains(passenger);
        assertThat(ride.getSeatsCount()).isZero();
        verify(rideCache).put(1L, ride);
    }

    @Test
    void removePassengerFromRide_shouldRemovePassenger_whenPassengerIsPresent() {
        Ride ride = new Ride();
        ride.setSeatsCount(1);
        User passenger = new User();
        ride.setPassengers(new java.util.ArrayList<>(List.of(passenger)));

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(userRepository.findById(2L)).thenReturn(Optional.of(passenger));
        when(rideRepository.save(ride)).thenReturn(ride);

        rideService.removePassengerFromRide(1L, 2L);

        assertThat(ride.getPassengers()).doesNotContain(passenger);
        assertThat(ride.getSeatsCount()).isEqualTo(2);
        verify(rideCache).put(1L, ride);
    }

    @Test
    void createRidesBulk_shouldCreateMultipleRides_whenDtosAreValid() {
        RideRequestDto dto1 = new RideRequestDto(1L, "Car1", 3, "CityA", "CityB", LocalDateTime.now(), "Note1");
        RideRequestDto dto2 = new RideRequestDto(2L, "Car2", 2, "CityX", "CityY", LocalDateTime.now().plusHours(1), "Note2");

        User driver1 = new User(); driver1.setId(1L);
        User driver2 = new User(); driver2.setId(2L);

        Ride ride1 = new Ride(); ride1.setId(10L); ride1.setDriver(driver1);
        Ride ride2 = new Ride(); ride2.setId(20L); ride2.setDriver(driver2);

        RideResponseDto response1 = new RideResponseDto(10L, "Car1", 3, "CityA", "CityB", dto1.departureTime(), "Note1", null, List.of());
        RideResponseDto response2 = new RideResponseDto(20L, "Car2", 2, "CityX", "CityY", dto2.departureTime(), "Note2", null, List.of());

        when(userRepository.findById(1L)).thenReturn(Optional.of(driver1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(driver2));

        when(rideMapper.toRide(dto1, driver1)).thenReturn(ride1);
        when(rideMapper.toRide(dto2, driver2)).thenReturn(ride2);

        when(rideRepository.save(ride1)).thenReturn(ride1);
        when(rideRepository.save(ride2)).thenReturn(ride2);

        when(rideMapper.toRideResponseDto(ride1)).thenReturn(response1);
        when(rideMapper.toRideResponseDto(ride2)).thenReturn(response2);

        List<RideResponseDto> result = rideService.createRides(List.of(dto1, dto2));

        assertThat(result).hasSize(2).containsExactly(response1, response2);

        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(rideRepository).save(ride1);
        verify(rideRepository).save(ride2);
    }
}
