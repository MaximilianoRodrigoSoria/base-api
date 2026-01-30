package com.ar.laboratory.baseapi.domain.ports.in;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;

import java.util.List;
import java.util.Optional;

/**
 * Input port defining use cases for ExampleStatus operations.
 * This interface represents the application's business logic entry points.
 */
public interface ExampleStatusUseCase {
    
    /**
     * Retrieves an ExampleStatus by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the ExampleStatus if found
     */
    Optional<ExampleStatus> getExampleStatusById(String id);
    
    /**
     * Retrieves all ExampleStatus entities.
     *
     * @return a list of all ExampleStatus entities
     */
    List<ExampleStatus> getAllExampleStatuses();
    
    /**
     * Retrieves all active ExampleStatus entities.
     *
     * @return a list of active ExampleStatus entities
     */
    List<ExampleStatus> getActiveExampleStatuses();
}
