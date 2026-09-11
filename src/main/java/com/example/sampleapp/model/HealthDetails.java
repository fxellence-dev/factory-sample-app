package com.example.sampleapp.model;

import java.time.Instant;

public class HealthDetails {

    private final String status;
    private final String service;
    private final Instant timestamp;

    public HealthDetails(String status, String service, Instant timestamp) {
        this.status = status;
        this.service = service;
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String getService() {
        return service;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
