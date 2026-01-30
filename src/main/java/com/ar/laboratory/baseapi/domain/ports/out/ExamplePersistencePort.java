package com.ar.laboratory.baseapi.domain.ports.out;

import com.ar.laboratory.baseapi.domain.model.Example;

import java.util.List;
import java.util.Optional;

/**
 * Output port for Example persistence operations.
 * This interface defines the contract for persistence operations in the domain layer.
 */
public interface ExamplePersistencePort {
    
    /**
     * Saves an Example entity.
     *
     * @param example the example to save
     * @return the saved example with generated ID
     */
    Example save(Example example);
    
    /**
     * Finds an Example by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the Example if found
     */
    Optional<Example> findById(Long id);
    
    /**
     * Finds an Example by DNI.
     *
     * @param dni the DNI to search for
     * @return an Optional containing the Example if found
     */
    Optional<Example> findByDni(String dni);
    
    /**
     * Retrieves all Example entities.
     *
     * @return a list of all examples
     */
    List<Example> findAll();
    
    /**
     * Checks if an Example exists by DNI.
     *
     * @param dni the DNI to check
     * @return true if exists, false otherwise
     */
    boolean existsByDni(String dni);
}
