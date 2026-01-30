package com.ar.laboratory.baseapi.domain.ports.out;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;

import java.util.List;
import java.util.Optional;

/**
 * Output port for ExampleStatus repository operations.
 * This interface defines the contract for persistence operations in the domain layer.
 * Implementations will be provided by adapters (e.g., InMemoryExampleRepository).
 */
public interface ExampleRepositoryPort {
    
    /**
     * Finds an ExampleStatus by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the ExampleStatus if found
     */
    Optional<ExampleStatus> findById(String id);
    
    /**
     * Retrieves all ExampleStatus entities.
     *
     * @return a list of all ExampleStatus entities
     */
    List<ExampleStatus> findAll();
    
    /**
     * Retrieves all active ExampleStatus entities.
     *
     * @return a list of active ExampleStatus entities
     */
    List<ExampleStatus> findAllActive();
    
    /**
     * Saves an ExampleStatus entity.
     *
     * @param exampleStatus the entity to save
     * @return the saved entity
     */
    ExampleStatus save(ExampleStatus exampleStatus);
}
