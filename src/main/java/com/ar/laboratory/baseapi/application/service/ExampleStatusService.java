package com.ar.laboratory.baseapi.application.service;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import com.ar.laboratory.baseapi.domain.ports.in.ExampleStatusUseCase;
import com.ar.laboratory.baseapi.domain.ports.out.CachePort;
import com.ar.laboratory.baseapi.domain.ports.out.ExampleRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service implementation for ExampleStatus use cases.
 * This class implements the business logic defined in ExampleStatusUseCase.
 * Integrates Redis caching for improved performance.
 */
@Service
public class ExampleStatusService implements ExampleStatusUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ExampleStatusService.class);
    
    private final ExampleRepositoryPort exampleRepositoryPort;
    private final CachePort<ExampleStatus> cachePort;

    public ExampleStatusService(ExampleRepositoryPort exampleRepositoryPort,
                               CachePort<ExampleStatus> cachePort) {
        this.exampleRepositoryPort = exampleRepositoryPort;
        this.cachePort = cachePort;
    }

    @Override
    public Optional<ExampleStatus> getExampleStatusById(String id) {
        logger.info("Retrieving example status by id: {}", id);
        
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Invalid id provided: {}", id);
            return Optional.empty();
        }
        
        // Try to get from cache first
        Optional<ExampleStatus> cached = cachePort.get(id);
        if (cached.isPresent()) {
            logger.debug("Found example status in cache: {}", cached.get().getName());
            return cached;
        }
        
        // If not in cache, get from repository
        Optional<ExampleStatus> result = exampleRepositoryPort.findById(id);
        
        if (result.isPresent()) {
            logger.debug("Found example status in repository: {}", result.get().getName());
            // Store in cache for future requests
            cachePort.put(id, result.get());
        } else {
            logger.debug("Example status not found for id: {}", id);
        }
        
        return result;
    }

    @Override
    public List<ExampleStatus> getAllExampleStatuses() {
        logger.info("Retrieving all example statuses");
        List<ExampleStatus> statuses = exampleRepositoryPort.findAll();
        logger.debug("Found {} example statuses", statuses.size());
        return statuses;
    }

    @Override
    public List<ExampleStatus> getActiveExampleStatuses() {
        logger.info("Retrieving active example statuses");
        List<ExampleStatus> activeStatuses = exampleRepositoryPort.findAllActive();
        logger.debug("Found {} active example statuses", activeStatuses.size());
        return activeStatuses;
    }
}
