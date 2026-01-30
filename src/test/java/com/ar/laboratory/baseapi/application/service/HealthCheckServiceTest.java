package com.ar.laboratory.baseapi.application.service;

import com.ar.laboratory.baseapi.domain.model.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HealthCheckService.
 */
@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @InjectMocks
    private HealthCheckService healthCheckService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(healthCheckService, "version", "1.0.0");
        ReflectionTestUtils.setField(healthCheckService, "applicationName", "base-api");
    }

    @Test
    void getHealthStatus_ShouldReturnHealthStatusWithCorrectFields() {
        // Act
        HealthStatus result = healthCheckService.getHealthStatus();

        // Assert
        assertNotNull(result);
        assertEquals("UP", result.getStatus());
        assertEquals("1.0.0", result.getVersion());
        assertEquals("base-api", result.getApplication());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void getHealthStatus_ShouldReturnCurrentTimestamp() {
        // Act
        HealthStatus result1 = healthCheckService.getHealthStatus();
        
        // Small delay
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        HealthStatus result2 = healthCheckService.getHealthStatus();

        // Assert
        assertNotNull(result1.getTimestamp());
        assertNotNull(result2.getTimestamp());
        assertTrue(result2.getTimestamp().isAfter(result1.getTimestamp()) || 
                   result2.getTimestamp().isEqual(result1.getTimestamp()));
    }
}
