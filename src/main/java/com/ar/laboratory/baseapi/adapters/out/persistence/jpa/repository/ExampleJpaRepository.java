package com.ar.laboratory.baseapi.adapters.out.persistence.jpa.repository;

import com.ar.laboratory.baseapi.adapters.out.persistence.jpa.entity.ExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for ExampleEntity.
 * This interface extends Spring Data JPA repository.
 */
@Repository
public interface ExampleJpaRepository extends JpaRepository<ExampleEntity, Long> {
    
    /**
     * Finds an Example by DNI.
     *
     * @param dni the DNI to search for
     * @return an Optional containing the ExampleEntity if found
     */
    Optional<ExampleEntity> findByDni(String dni);
    
    /**
     * Checks if an Example exists by DNI.
     *
     * @param dni the DNI to check
     * @return true if exists, false otherwise
     */
    boolean existsByDni(String dni);
}
