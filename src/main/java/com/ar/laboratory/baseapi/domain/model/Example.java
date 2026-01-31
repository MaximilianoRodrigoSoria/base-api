package com.ar.laboratory.baseapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Domain model representing an Example entity.
 * This class is part of the domain layer and is framework-independent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Example implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String genero;
    private String cuit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
