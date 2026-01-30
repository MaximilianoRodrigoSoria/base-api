package com.ar.laboratory.baseapi.adapters.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ExampleStatus response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleStatusResponse {
    
    private String id;
    private String name;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private boolean active;
}
