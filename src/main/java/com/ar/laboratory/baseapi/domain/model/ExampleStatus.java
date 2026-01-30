package com.ar.laboratory.baseapi.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model representing an example status entity.
 * This class is part of the domain layer and is framework-independent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleStatus {
    
    private String id;
    private String name;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private boolean active;
}
