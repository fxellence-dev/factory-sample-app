package com.example.sampleapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthDetails_returnsUpStatusWithServiceNameAndTimestamp() throws Exception {
        mockMvc.perform(get("/health/details"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("sample-app-springboot"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void healthDetails_timestampIsFreshWithinRequestBoundaries() throws Exception {
        Instant before = Instant.now();
        String responseBody = mockMvc.perform(get("/health/details"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Instant after = Instant.now();

        JsonNode json = new ObjectMapper().readTree(responseBody);
        Instant timestamp = Instant.parse(json.path("timestamp").asText());

        Duration tolerance = Duration.ofSeconds(1);
        Instant lowerBound = before.minus(tolerance);
        Instant upperBound = after.plus(tolerance);

        assertTrue(!timestamp.isBefore(lowerBound) && !timestamp.isAfter(upperBound),
                "Expected timestamp to be within request-time boundaries");
    }

    @Test
    void healthDetails_postReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/health/details"))
                .andExpect(status().isMethodNotAllowed());
    }
}
