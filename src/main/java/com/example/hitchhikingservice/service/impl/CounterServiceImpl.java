package com.example.hitchhikingservice.service.impl;

import org.springframework.stereotype.Service;
import com.example.hitchhikingservice.service.CounterService;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class CounterServiceImpl implements CounterService {
    private final AtomicLong counter = new AtomicLong(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public long getTotalVisits() {
        return counter.get();
    }
}
