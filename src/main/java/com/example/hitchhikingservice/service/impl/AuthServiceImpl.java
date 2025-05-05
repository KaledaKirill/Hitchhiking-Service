package com.example.hitchhikingservice.service.impl;

import com.example.hitchhikingservice.model.dto.request.AuthRequestDto;
import com.example.hitchhikingservice.model.dto.request.LoginRequestDto;
import com.example.hitchhikingservice.model.dto.response.AuthResponseDto;
import com.example.hitchhikingservice.model.entity.Role;
import com.example.hitchhikingservice.model.entity.User;
import com.example.hitchhikingservice.repository.UserRepository;
import com.example.hitchhikingservice.security.JwtService;
import com.example.hitchhikingservice.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDto register(AuthRequestDto request) {
        // Проверка на существование пользователя с таким email
        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("User with this email already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new IllegalStateException("Invalid email or password", e);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        return new AuthResponseDto(token);
    }
}