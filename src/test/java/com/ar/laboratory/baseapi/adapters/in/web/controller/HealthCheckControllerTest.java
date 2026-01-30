package com.ar.laboratory.baseapi.adapters.in.web.controller;

import com.ar.laboratory.baseapi.domain.model.HealthStatus;
import com.ar.laboratory.baseapi.domain.ports.in.HealthCheckUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for HealthCheckController using MockMvc.
 */
@WebMvcTest(HealthCheckController.class)
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckUseCase healthCheckUseCase;

    @Test
    void checkHealth_ShouldReturnHealthStatus() throws Exception {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(timestamp)
                .version("1.0.0")
                .application("base-api")
                .build();

        when(healthCheckUseCase.getHealthStatus()).thenReturn(healthStatus);

        // Act & Assert
        mockMvc.perform(get("/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.version", is("1.0.0")))
                .andExpect(jsonPath("$.application", is("base-api")))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(healthCheckUseCase, times(1)).getHealthStatus();
    }

    @Test
    void checkHealth_ShouldReturn200StatusCode() throws Exception {
        // Arrange
        HealthStatus healthStatus = HealthStatus.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .version("1.0.0")
                .application("base-api")
                .build();

        when(healthCheckUseCase.getHealthStatus()).thenReturn(healthStatus);

        // Act & Assert
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());

        verify(healthCheckUseCase, times(1)).getHealthStatus();
    }
}
