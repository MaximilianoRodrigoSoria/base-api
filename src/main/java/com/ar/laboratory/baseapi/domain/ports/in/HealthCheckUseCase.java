package com.ar.laboratory.baseapi.domain.ports.in;

import com.ar.laboratory.baseapi.domain.model.HealthStatus;

/**
 * Input port defining use cases for health check operations.
 */
public interface HealthCheckUseCase {
    
    /**
     * Retrieves the current health status of the application.
     *
     * @return the HealthStatus object
     */
    HealthStatus getHealthStatus();
}
