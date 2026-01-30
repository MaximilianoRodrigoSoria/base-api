package com.ar.laboratory.baseapi.domain.ports.in;

import com.ar.laboratory.baseapi.domain.model.Example;

/**
 * Input port for creating Example entities.
 * This interface represents the use case for creating examples.
 */
public interface CreateExampleUseCase {
    
    /**
     * Creates a new Example.
     *
     * @param example the example to create
     * @return the created example with generated ID
     */
    Example createExample(Example example);
    
    /**
     * Finds an Example by DNI.
     *
     * @param dni the DNI to search for
     * @return the Example if found, null otherwise
     */
    Example findByDni(String dni);
}
