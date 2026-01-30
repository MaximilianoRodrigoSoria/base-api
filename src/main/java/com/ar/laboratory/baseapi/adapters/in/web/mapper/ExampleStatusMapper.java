package com.ar.laboratory.baseapi.adapters.in.web.mapper;

import com.ar.laboratory.baseapi.adapters.in.web.dto.ExampleStatusResponse;
import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between ExampleStatus domain model and DTOs.
 */
@Component
public class ExampleStatusMapper {

    public ExampleStatusResponse toResponse(ExampleStatus exampleStatus) {
        if (exampleStatus == null) {
            return null;
        }
        
        return ExampleStatusResponse.builder()
                .id(exampleStatus.getId())
                .name(exampleStatus.getName())
                .status(exampleStatus.getStatus())
                .description(exampleStatus.getDescription())
                .createdAt(exampleStatus.getCreatedAt())
                .active(exampleStatus.isActive())
                .build();
    }

    public List<ExampleStatusResponse> toResponseList(List<ExampleStatus> exampleStatuses) {
        if (exampleStatuses == null) {
            return List.of();
        }
        
        return exampleStatuses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
