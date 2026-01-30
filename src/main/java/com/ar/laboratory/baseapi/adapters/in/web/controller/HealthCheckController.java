package com.ar.laboratory.baseapi.adapters.in.web.controller;

import com.ar.laboratory.baseapi.adapters.in.web.dto.HealthCheckResponse;
import com.ar.laboratory.baseapi.domain.model.HealthStatus;
import com.ar.laboratory.baseapi.domain.ports.in.HealthCheckUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for health check operations.
 * This is an input adapter in the hexagonal architecture.
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Health check operations")
public class HealthCheckController {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    
    private final HealthCheckUseCase healthCheckUseCase;

    public HealthCheckController(HealthCheckUseCase healthCheckUseCase) {
        this.healthCheckUseCase = healthCheckUseCase;
    }

    @GetMapping
    @Operation(
        summary = "Check application health",
        description = "Returns the current health status of the application including version and timestamp"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application is healthy")
    })
    public ResponseEntity<HealthCheckResponse> checkHealth() {
        logger.info("Health check endpoint called");
        
        HealthStatus healthStatus = healthCheckUseCase.getHealthStatus();
        
        HealthCheckResponse response = HealthCheckResponse.builder()
                .status(healthStatus.getStatus())
                .timestamp(healthStatus.getTimestamp())
                .version(healthStatus.getVersion())
                .application(healthStatus.getApplication())
                .build();
        
        logger.debug("Health check response: {}", response);
        
        return ResponseEntity.ok(response);
    }
}
