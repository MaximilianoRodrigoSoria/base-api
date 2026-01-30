package com.ar.laboratory.baseapi.adapters.out.cache;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisCacheAdapter.
 */
@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisCacheAdapter redisCacheAdapter;

    private ExampleStatus testStatus;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        testStatus = ExampleStatus.builder()
                .id("1")
                .name("Test Service")
                .status("RUNNING")
                .description("Test description")
                .createdAt(LocalDateTime.now())
                .active(true)
                .build();
    }

    @Test
    void get_ShouldReturnValue_WhenKeyExists() {
        // Arrange
        String key = "1";
        String fullKey = "example-status:1";
        when(valueOperations.get(fullKey)).thenReturn(testStatus);

        // Act
        Optional<ExampleStatus> result = redisCacheAdapter.get(key);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Service", result.get().getName());
        verify(valueOperations, times(1)).get(fullKey);
    }

    @Test
    void get_ShouldReturnEmpty_WhenKeyDoesNotExist() {
        // Arrange
        String key = "999";
        String fullKey = "example-status:999";
        when(valueOperations.get(fullKey)).thenReturn(null);

        // Act
        Optional<ExampleStatus> result = redisCacheAdapter.get(key);

        // Assert
        assertFalse(result.isPresent());
        verify(valueOperations, times(1)).get(fullKey);
    }

    @Test
    void get_ShouldReturnEmpty_WhenExceptionOccurs() {
        // Arrange
        String key = "1";
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Act
        Optional<ExampleStatus> result = redisCacheAdapter.get(key);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void put_ShouldStoreValue() {
        // Arrange
        String key = "1";
        String fullKey = "example-status:1";

        // Act
        redisCacheAdapter.put(key, testStatus);

        // Assert
        verify(valueOperations, times(1)).set(
                eq(fullKey),
                eq(testStatus),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void put_ShouldHandleException() {
        // Arrange
        String key = "1";
        doThrow(new RuntimeException("Redis error"))
                .when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        // Act & Assert (no exception should be thrown)
        assertDoesNotThrow(() -> redisCacheAdapter.put(key, testStatus));
    }

    @Test
    void evict_ShouldDeleteKey() {
        // Arrange
        String key = "1";
        String fullKey = "example-status:1";
        when(redisTemplate.delete(fullKey)).thenReturn(true);

        // Act
        redisCacheAdapter.evict(key);

        // Assert
        verify(redisTemplate, times(1)).delete(fullKey);
    }

    @Test
    void evict_ShouldHandleNonExistentKey() {
        // Arrange
        String key = "999";
        String fullKey = "example-status:999";
        when(redisTemplate.delete(fullKey)).thenReturn(false);

        // Act
        redisCacheAdapter.evict(key);

        // Assert
        verify(redisTemplate, times(1)).delete(fullKey);
    }

    @Test
    void clear_ShouldDeleteAllKeys() {
        // Arrange
        Set<String> keys = Set.of("example-status:1", "example-status:2");
        when(redisTemplate.keys("example-status:*")).thenReturn(keys);
        when(redisTemplate.delete(keys)).thenReturn(2L);

        // Act
        redisCacheAdapter.clear();

        // Assert
        verify(redisTemplate, times(1)).keys("example-status:*");
        verify(redisTemplate, times(1)).delete(keys);
    }

    @Test
    void clear_ShouldHandleNoKeys() {
        // Arrange
        when(redisTemplate.keys("example-status:*")).thenReturn(Set.of());

        // Act
        redisCacheAdapter.clear();

        // Assert
        verify(redisTemplate, times(1)).keys("example-status:*");
        verify(redisTemplate, never()).delete(anySet());
    }

    @Test
    void exists_ShouldReturnTrue_WhenKeyExists() {
        // Arrange
        String key = "1";
        String fullKey = "example-status:1";
        when(redisTemplate.hasKey(fullKey)).thenReturn(true);

        // Act
        boolean result = redisCacheAdapter.exists(key);

        // Assert
        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey(fullKey);
    }

    @Test
    void exists_ShouldReturnFalse_WhenKeyDoesNotExist() {
        // Arrange
        String key = "999";
        String fullKey = "example-status:999";
        when(redisTemplate.hasKey(fullKey)).thenReturn(false);

        // Act
        boolean result = redisCacheAdapter.exists(key);

        // Assert
        assertFalse(result);
        verify(redisTemplate, times(1)).hasKey(fullKey);
    }

    @Test
    void exists_ShouldReturnFalse_WhenExceptionOccurs() {
        // Arrange
        String key = "1";
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Act
        boolean result = redisCacheAdapter.exists(key);

        // Assert
        assertFalse(result);
    }
}
