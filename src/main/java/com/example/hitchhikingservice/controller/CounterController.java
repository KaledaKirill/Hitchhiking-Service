package com.example.hitchhikingservice.controller;

import com.example.hitchhikingservice.service.CounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
@Tag(name = "Visit tracking", description = "Visit counter operations")
public class CounterController {

    private final CounterService counterService;

    @GetMapping
    @Operation(summary = "Register visit", description = "Increments visit counter")
    public void regVisit() {
        counterService.increment();
    }

    @GetMapping("/count")
    @Operation(summary = "Get visit count", description = "Returns visit amount")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVisits", counterService.getTotalVisits());

        return ResponseEntity.ok(stats);
    }
}
