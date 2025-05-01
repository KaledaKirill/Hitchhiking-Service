package com.example.hitchhikingservice.repository;

import com.example.hitchhikingservice.model.entity.Ride;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findAllByDriverId(Long driverId);

    List<Ride> findAllByDriverName(String name);

    @Query("SELECT r FROM Ride r JOIN r.passengers p WHERE p.id = :passengerId")
    List<Ride> findAllByPassengerId(Long passengerId);

    @Query(value = "SELECT r.* FROM rides r "
        + "JOIN ride_passengers rp ON r.id = rp.ride_id "
        + "JOIN users p ON rp.user_id = p.id "
        + "WHERE p.name = :passengerName", nativeQuery = true)
    List<Ride> findAllByPassengerName(String passengerName);

}
