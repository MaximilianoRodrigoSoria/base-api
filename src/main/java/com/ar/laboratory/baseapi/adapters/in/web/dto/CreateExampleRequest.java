package com.ar.laboratory.baseapi.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Example.
 * Includes validation annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExampleRequest {
    
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String apellido;
    
    @NotBlank(message = "El DNI no puede estar vacío")
    @Pattern(regexp = "^[0-9]{7,8}$", message = "El DNI debe tener 7 u 8 dígitos numéricos")
    private String dni;
    
    @NotBlank(message = "El género no puede estar vacío")
    @Pattern(regexp = "^[HM]$", message = "El género debe ser H (Hombre) o M (Mujer)")
    private String genero;
}
