package com.ar.laboratory.baseapi.application.service;

import com.ar.laboratory.baseapi.domain.model.Example;
import com.ar.laboratory.baseapi.domain.ports.in.CreateExampleUseCase;
import com.ar.laboratory.baseapi.domain.ports.out.CuitServicePort;
import com.ar.laboratory.baseapi.domain.ports.out.ExamplePersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service implementation for Example use cases.
 * This class implements the business logic for creating and managing examples.
 */
@Service
@Transactional
public class ExampleService implements CreateExampleUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ExampleService.class);
    
    private final ExamplePersistencePort examplePersistencePort;
    private final CuitServicePort cuitServicePort;

    public ExampleService(ExamplePersistencePort examplePersistencePort,
                         CuitServicePort cuitServicePort) {
        this.examplePersistencePort = examplePersistencePort;
        this.cuitServicePort = cuitServicePort;
    }

    @Override
    public Example createExample(Example example) {
        logger.info("Creating example for DNI: {}", example.getDni());
        
        // Validate business rules
        if (examplePersistencePort.existsByDni(example.getDni())) {
            logger.warn("Example with DNI {} already exists", example.getDni());
            throw new IllegalArgumentException("Ya existe un ejemplo con el DNI: " + example.getDni());
        }
        
        // Calculate CUIT using external service
        String cuit = cuitServicePort.getCuit(example.getDni(), example.getGenero());
        example.setCuit(cuit);
        logger.info("CUIT calculated: {}", cuit);
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        example.setCreatedAt(now);
        example.setUpdatedAt(now);
        
        // Save example
        Example savedExample = examplePersistencePort.save(example);
        
        logger.info("Example created successfully with ID: {}", savedExample.getId());
        
        return savedExample;
    }

    @Override
    @Transactional(readOnly = true)
    public Example findByDni(String dni) {
        logger.info("Finding example by DNI: {}", dni);
        
        return examplePersistencePort.findByDni(dni)
                .orElse(null);
    }
}
