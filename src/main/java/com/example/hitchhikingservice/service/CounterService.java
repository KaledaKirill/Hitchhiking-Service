package com.example.hitchhikingservice.service;

import java.util.Map;

public interface CounterService {
    void increment(String uri);

    Map<String, Long> getStats();
}
