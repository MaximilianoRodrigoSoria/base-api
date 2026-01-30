package com.ar.laboratory.baseapi.domain.ports.out;

import java.util.Optional;

/**
 * Output port for cache operations.
 * This interface defines the contract for caching operations in the domain layer.
 */
public interface CachePort<T> {
    
    /**
     * Retrieves a value from the cache.
     *
     * @param key the cache key
     * @return an Optional containing the cached value if found
     */
    Optional<T> get(String key);
    
    /**
     * Stores a value in the cache.
     *
     * @param key the cache key
     * @param value the value to cache
     */
    void put(String key, T value);
    
    /**
     * Removes a value from the cache.
     *
     * @param key the cache key
     */
    void evict(String key);
    
    /**
     * Clears all values from the cache.
     */
    void clear();
    
    /**
     * Checks if a key exists in the cache.
     *
     * @param key the cache key
     * @return true if the key exists, false otherwise
     */
    boolean exists(String key);
}
