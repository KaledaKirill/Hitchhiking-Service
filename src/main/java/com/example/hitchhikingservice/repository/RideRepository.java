package com.example.hitchhikingservice.repository;

import com.example.hitchhikingservice.model.entity.Ride;
import com.example.hitchhikingservice.model.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findAllByDriver(User driver);

    @Query("SELECT r FROM Ride r JOIN r.passengers p WHERE p = :passenger")
    List<Ride> findAllByPassenger(User passenger);
}
