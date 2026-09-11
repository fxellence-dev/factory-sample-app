package com.example.sampleapp.controller;

import com.example.sampleapp.model.HealthDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HealthController {

    private final String applicationName;

    public HealthController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/health/details")
    public HealthDetails getHealthDetails() {
        return new HealthDetails("UP", applicationName, Instant.now());
    }
}
