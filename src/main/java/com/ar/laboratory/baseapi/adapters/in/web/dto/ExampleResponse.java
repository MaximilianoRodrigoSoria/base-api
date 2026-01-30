package com.ar.laboratory.baseapi.adapters.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Example response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleResponse {
    
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
