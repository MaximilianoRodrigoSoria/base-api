package com.ar.laboratory.baseapi.application.service;

import com.ar.laboratory.baseapi.domain.model.HealthStatus;
import com.ar.laboratory.baseapi.domain.ports.in.HealthCheckUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service implementation for health check operations.
 */
@Service
public class HealthCheckService implements HealthCheckUseCase {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    
    @Value("${app.version}")
    private String version;
    
    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public HealthStatus getHealthStatus() {
        logger.debug("Health check requested");
        
        return HealthStatus.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .version(version)
                .application(applicationName)
                .build();
    }
}
