package com.example.hitchhikingservice.service;

import org.springframework.core.io.Resource;

public interface LogService {
    public Resource getLogsByDate(String date);
}
