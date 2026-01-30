package com.ar.laboratory.baseapi.adapters.out.persistence.jpa;

import com.ar.laboratory.baseapi.adapters.out.persistence.jpa.entity.ExampleEntity;
import com.ar.laboratory.baseapi.adapters.out.persistence.jpa.repository.ExampleJpaRepository;
import com.ar.laboratory.baseapi.domain.model.Example;
import com.ar.laboratory.baseapi.domain.ports.out.ExamplePersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of ExamplePersistencePort.
 * This is an output adapter that implements persistence using JPA.
 */
@Component
public class ExampleJpaAdapter implements ExamplePersistencePort {

    private static final Logger logger = LoggerFactory.getLogger(ExampleJpaAdapter.class);
    
    private final ExampleJpaRepository exampleJpaRepository;

    public ExampleJpaAdapter(ExampleJpaRepository exampleJpaRepository) {
        this.exampleJpaRepository = exampleJpaRepository;
    }

    @Override
    public Example save(Example example) {
        logger.debug("Saving example with DNI: {}", example.getDni());
        
        ExampleEntity entity = toEntity(example);
        ExampleEntity savedEntity = exampleJpaRepository.save(entity);
        
        logger.debug("Example saved with ID: {}", savedEntity.getId());
        
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Example> findById(Long id) {
        logger.debug("Finding example by ID: {}", id);
        
        return exampleJpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<Example> findByDni(String dni) {
        logger.debug("Finding example by DNI: {}", dni);
        
        return exampleJpaRepository.findByDni(dni)
                .map(this::toDomain);
    }

    @Override
    public List<Example> findAll() {
        logger.debug("Finding all examples");
        
        return exampleJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByDni(String dni) {
        logger.debug("Checking if example exists by DNI: {}", dni);
        
        return exampleJpaRepository.existsByDni(dni);
    }

    // Mapper methods
    private ExampleEntity toEntity(Example example) {
        return ExampleEntity.builder()
                .id(example.getId())
                .nombre(example.getNombre())
                .apellido(example.getApellido())
                .dni(example.getDni())
                .createdAt(example.getCreatedAt())
                .updatedAt(example.getUpdatedAt())
                .build();
    }

    private Example toDomain(ExampleEntity entity) {
        return Example.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .apellido(entity.getApellido())
                .dni(entity.getDni())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
