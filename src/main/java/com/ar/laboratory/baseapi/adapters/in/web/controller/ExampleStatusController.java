package com.ar.laboratory.baseapi.adapters.in.web.controller;

import com.ar.laboratory.baseapi.adapters.in.web.dto.ExampleStatusResponse;
import com.ar.laboratory.baseapi.adapters.in.web.mapper.ExampleStatusMapper;
import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import com.ar.laboratory.baseapi.domain.ports.in.ExampleStatusUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for ExampleStatus operations.
 * This is an input adapter demonstrating hexagonal architecture.
 */
@RestController
@RequestMapping("/example-status")
@Tag(name = "Example Status", description = "Example status operations demonstrating hexagonal architecture")
public class ExampleStatusController {

    private static final Logger logger = LoggerFactory.getLogger(ExampleStatusController.class);
    
    private final ExampleStatusUseCase exampleStatusUseCase;
    private final ExampleStatusMapper exampleStatusMapper;

    public ExampleStatusController(ExampleStatusUseCase exampleStatusUseCase, 
                                   ExampleStatusMapper exampleStatusMapper) {
        this.exampleStatusUseCase = exampleStatusUseCase;
        this.exampleStatusMapper = exampleStatusMapper;
    }

    @GetMapping
    @Operation(
        summary = "Get all example statuses",
        description = "Retrieves all example statuses from the in-memory repository"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all statuses")
    })
    public ResponseEntity<List<ExampleStatusResponse>> getAllStatuses() {
        logger.info("GET /example-status - Retrieving all statuses");
        
        List<ExampleStatus> statuses = exampleStatusUseCase.getAllExampleStatuses();
        List<ExampleStatusResponse> response = exampleStatusMapper.toResponseList(statuses);
        
        logger.debug("Returning {} example statuses", response.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(
        summary = "Get active example statuses",
        description = "Retrieves only active example statuses from the in-memory repository"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active statuses")
    })
    public ResponseEntity<List<ExampleStatusResponse>> getActiveStatuses() {
        logger.info("GET /example-status/active - Retrieving active statuses");
        
        List<ExampleStatus> activeStatuses = exampleStatusUseCase.getActiveExampleStatuses();
        List<ExampleStatusResponse> response = exampleStatusMapper.toResponseList(activeStatuses);
        
        logger.debug("Returning {} active example statuses", response.size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get example status by ID",
        description = "Retrieves a specific example status by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the status"),
        @ApiResponse(responseCode = "404", description = "Status not found")
    })
    public ResponseEntity<ExampleStatusResponse> getStatusById(
            @Parameter(description = "ID of the status to retrieve", required = true)
            @PathVariable String id) {
        logger.info("GET /example-status/{} - Retrieving status by id", id);
        
        return exampleStatusUseCase.getExampleStatusById(id)
                .map(exampleStatusMapper::toResponse)
                .map(response -> {
                    logger.debug("Found example status: {}", response.getName());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Example status not found for id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }
}
