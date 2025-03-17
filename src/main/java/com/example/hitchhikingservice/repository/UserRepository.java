package com.example.hitchhikingservice.repository;

import com.example.hitchhikingservice.model.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByName(String name);
}
