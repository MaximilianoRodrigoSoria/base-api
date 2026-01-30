package com.ar.laboratory.baseapi.application.service;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import com.ar.laboratory.baseapi.domain.ports.out.ExampleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExampleStatusService.
 */
@ExtendWith(MockitoExtension.class)
class ExampleStatusServiceTest {

    @Mock
    private ExampleRepositoryPort exampleRepositoryPort;

    @InjectMocks
    private ExampleStatusService exampleStatusService;

    private ExampleStatus testStatus1;
    private ExampleStatus testStatus2;
    private ExampleStatus testStatus3;

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

        testStatus3 = ExampleStatus.builder()
                .id("3")
                .name("Test Service C")
                .status("STOPPED")
                .description("Test description C")
                .createdAt(LocalDateTime.now())
                .active(false)
                .build();
    }

    @Test
    void getExampleStatusById_ShouldReturnStatus_WhenIdExists() {
        // Arrange
        String id = "1";
        when(exampleRepositoryPort.findById(id)).thenReturn(Optional.of(testStatus1));

        // Act
        Optional<ExampleStatus> result = exampleStatusService.getExampleStatusById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        assertEquals("Test Service A", result.get().getName());
        assertEquals("RUNNING", result.get().getStatus());
        verify(exampleRepositoryPort, times(1)).findById(id);
    }

    @Test
    void getExampleStatusById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // Arrange
        String id = "999";
        when(exampleRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // Act
        Optional<ExampleStatus> result = exampleStatusService.getExampleStatusById(id);

        // Assert
        assertFalse(result.isPresent());
        verify(exampleRepositoryPort, times(1)).findById(id);
    }

    @Test
    void getExampleStatusById_ShouldReturnEmpty_WhenIdIsNull() {
        // Act
        Optional<ExampleStatus> result = exampleStatusService.getExampleStatusById(null);

        // Assert
        assertFalse(result.isPresent());
        verify(exampleRepositoryPort, never()).findById(any());
    }

    @Test
    void getExampleStatusById_ShouldReturnEmpty_WhenIdIsEmpty() {
        // Act
        Optional<ExampleStatus> result = exampleStatusService.getExampleStatusById("   ");

        // Assert
        assertFalse(result.isPresent());
        verify(exampleRepositoryPort, never()).findById(any());
    }

    @Test
    void getAllExampleStatuses_ShouldReturnAllStatuses() {
        // Arrange
        List<ExampleStatus> allStatuses = Arrays.asList(testStatus1, testStatus2, testStatus3);
        when(exampleRepositoryPort.findAll()).thenReturn(allStatuses);

        // Act
        List<ExampleStatus> result = exampleStatusService.getAllExampleStatuses();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Test Service A", result.get(0).getName());
        assertEquals("Test Service B", result.get(1).getName());
        assertEquals("Test Service C", result.get(2).getName());
        verify(exampleRepositoryPort, times(1)).findAll();
    }

    @Test
    void getAllExampleStatuses_ShouldReturnEmptyList_WhenNoStatuses() {
        // Arrange
        when(exampleRepositoryPort.findAll()).thenReturn(List.of());

        // Act
        List<ExampleStatus> result = exampleStatusService.getAllExampleStatuses();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(exampleRepositoryPort, times(1)).findAll();
    }

    @Test
    void getActiveExampleStatuses_ShouldReturnOnlyActiveStatuses() {
        // Arrange
        List<ExampleStatus> activeStatuses = Arrays.asList(testStatus1, testStatus2);
        when(exampleRepositoryPort.findAllActive()).thenReturn(activeStatuses);

        // Act
        List<ExampleStatus> result = exampleStatusService.getActiveExampleStatuses();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isActive());
        assertTrue(result.get(1).isActive());
        assertEquals("Test Service A", result.get(0).getName());
        assertEquals("Test Service B", result.get(1).getName());
        verify(exampleRepositoryPort, times(1)).findAllActive();
    }

    @Test
    void getActiveExampleStatuses_ShouldReturnEmptyList_WhenNoActiveStatuses() {
        // Arrange
        when(exampleRepositoryPort.findAllActive()).thenReturn(List.of());

        // Act
        List<ExampleStatus> result = exampleStatusService.getActiveExampleStatuses();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(exampleRepositoryPort, times(1)).findAllActive();
    }
}
