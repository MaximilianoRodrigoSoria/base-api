package com.ar.laboratory.baseapi.adapters.in.web.controller;

import com.ar.laboratory.baseapi.adapters.in.web.mapper.ExampleStatusMapper;
import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import com.ar.laboratory.baseapi.domain.ports.in.ExampleStatusUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ExampleStatusController using MockMvc.
 */
@WebMvcTest(ExampleStatusController.class)
class ExampleStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExampleStatusUseCase exampleStatusUseCase;

    @MockBean
    private ExampleStatusMapper exampleStatusMapper;

    private ExampleStatus testStatus1;
    private ExampleStatus testStatus2;

    @BeforeEach
    void setUp() {
        testStatus1 = ExampleStatus.builder()
                .id("1")
                .name("Test Service A")
                .status("RUNNING")
                .description("Test description A")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();

        testStatus2 = ExampleStatus.builder()
                .id("2")
                .name("Test Service B")
                .status("IDLE")
                .description("Test description B")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    void getAllStatuses_ShouldReturnListOfStatuses() throws Exception {
        // Arrange
        List<ExampleStatus> statuses = Arrays.asList(testStatus1, testStatus2);
        when(exampleStatusUseCase.getAllExampleStatuses()).thenReturn(statuses);
        when(exampleStatusMapper.toResponseList(statuses)).thenCallRealMethod();
        when(exampleStatusMapper.toResponse(any())).thenCallRealMethod();

        // Act & Assert
        mockMvc.perform(get("/example-status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].name", is("Test Service A")))
                .andExpect(jsonPath("$[0].status", is("RUNNING")))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].name", is("Test Service B")));

        verify(exampleStatusUseCase, times(1)).getAllExampleStatuses();
    }

    @Test
    void getActiveStatuses_ShouldReturnOnlyActiveStatuses() throws Exception {
        // Arrange
        List<ExampleStatus> activeStatuses = List.of(testStatus1, testStatus2);
        when(exampleStatusUseCase.getActiveExampleStatuses()).thenReturn(activeStatuses);
        when(exampleStatusMapper.toResponseList(activeStatuses)).thenCallRealMethod();
        when(exampleStatusMapper.toResponse(any())).thenCallRealMethod();

        // Act & Assert
        mockMvc.perform(get("/example-status/active")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].active", is(true)))
                .andExpect(jsonPath("$[1].active", is(true)));

        verify(exampleStatusUseCase, times(1)).getActiveExampleStatuses();
    }

    @Test
    void getStatusById_ShouldReturnStatus_WhenIdExists() throws Exception {
        // Arrange
        String id = "1";
        when(exampleStatusUseCase.getExampleStatusById(id)).thenReturn(Optional.of(testStatus1));
        when(exampleStatusMapper.toResponse(testStatus1)).thenCallRealMethod();

        // Act & Assert
        mockMvc.perform(get("/example-status/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Test Service A")))
                .andExpect(jsonPath("$.status", is("RUNNING")))
                .andExpect(jsonPath("$.description", is("Test description A")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(exampleStatusUseCase, times(1)).getExampleStatusById(id);
    }

    @Test
    void getStatusById_ShouldReturn404_WhenIdDoesNotExist() throws Exception {
        // Arrange
        String id = "999";
        when(exampleStatusUseCase.getExampleStatusById(id)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/example-status/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(exampleStatusUseCase, times(1)).getExampleStatusById(id);
    }
}
