package com.example.hitchhikingservice.service;

import com.example.hitchhikingservice.model.dto.request.AuthRequestDto;
import com.example.hitchhikingservice.model.dto.request.LoginRequestDto;
import com.example.hitchhikingservice.model.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto register(AuthRequestDto request);

    AuthResponseDto login(LoginRequestDto request);
}
