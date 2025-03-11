package com.example.hitchhikingservice.service.mapper;

import com.example.hitchhikingservice.model.dto.request.RideRequestDto;
import com.example.hitchhikingservice.model.dto.response.RideResponseDto;
import com.example.hitchhikingservice.model.entity.Ride;
import com.example.hitchhikingservice.model.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RideMapper {

    private final UserMapper userMapper;

    public RideResponseDto toRideResponseDto(Ride ride) {
        return new RideResponseDto(
                ride.getId(),
                ride.getCar(),
                ride.getSeatsCount(),
                ride.getDeparture(),
                ride.getDestination(),
                ride.getDepartureTime(),
                ride.getComment(),
                userMapper.toUserResponseDto(ride.getDriver()),
                ride.getPassengers().stream().map(userMapper::toUserResponseDto).toList()
        );
    }

    public Ride toRide(RideRequestDto dto, User driver) {
        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setCar(dto.car());
        ride.setSeatsCount(dto.seatsCount());
        ride.setDeparture(dto.departure());
        ride.setDestination(dto.destination());
        ride.setDepartureTime(dto.departureTime());
        ride.setComment(dto.comment());
        return ride;
    }
}

