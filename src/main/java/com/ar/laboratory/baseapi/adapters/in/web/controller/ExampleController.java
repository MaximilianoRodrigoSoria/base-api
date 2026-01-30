package com.ar.laboratory.baseapi.adapters.in.web.controller;

import com.ar.laboratory.baseapi.adapters.in.web.dto.CreateExampleRequest;
import com.ar.laboratory.baseapi.adapters.in.web.dto.ExampleResponse;
import com.ar.laboratory.baseapi.domain.model.Example;
import com.ar.laboratory.baseapi.domain.ports.in.CreateExampleUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Example operations.
 * This is an input adapter demonstrating hexagonal architecture with JPA.
 */
@RestController
@RequestMapping("/examples")
@Tag(name = "Examples", description = "Operations for managing examples with database persistence")
public class ExampleController {

    private static final Logger logger = LoggerFactory.getLogger(ExampleController.class);
    
    private final CreateExampleUseCase createExampleUseCase;

    public ExampleController(CreateExampleUseCase createExampleUseCase) {
        this.createExampleUseCase = createExampleUseCase;
    }

    @PostMapping
    @Operation(
        summary = "Create a new example",
        description = "Creates a new example with nombre, apellido, and DNI. DNI must be unique."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Example created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or DNI already exists"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ExampleResponse> createExample(@Valid @RequestBody CreateExampleRequest request) {
        logger.info("POST /examples - Creating example with DNI: {}", request.getDni());
        
        try {
            Example example = Example.builder()
                    .nombre(request.getNombre())
                    .apellido(request.getApellido())
                    .dni(request.getDni())
                    .build();
            
            Example createdExample = createExampleUseCase.createExample(example);
            
            ExampleResponse response = toResponse(createdExample);
            
            logger.info("Example created successfully with ID: {}", response.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create example: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping("/dni/{dni}")
    @Operation(
        summary = "Find example by DNI",
        description = "Retrieves an example by its DNI"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Example found"),
        @ApiResponse(responseCode = "404", description = "Example not found")
    })
    public ResponseEntity<ExampleResponse> findByDni(@PathVariable String dni) {
        logger.info("GET /examples/dni/{} - Finding example by DNI", dni);
        
        Example example = createExampleUseCase.findByDni(dni);
        
        if (example == null) {
            logger.info("Example not found for DNI: {}", dni);
            return ResponseEntity.notFound().build();
        }
        
        ExampleResponse response = toResponse(example);
        
        return ResponseEntity.ok(response);
    }

    private ExampleResponse toResponse(Example example) {
        return ExampleResponse.builder()
                .id(example.getId())
                .nombre(example.getNombre())
                .apellido(example.getApellido())
                .dni(example.getDni())
                .createdAt(example.getCreatedAt())
                .updatedAt(example.getUpdatedAt())
                .build();
    }
}
