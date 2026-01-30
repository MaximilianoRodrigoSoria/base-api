package com.ar.laboratory.baseapi.adapters.out.cache;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import com.ar.laboratory.baseapi.domain.ports.out.CachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis implementation of CachePort.
 * This is an output adapter that provides caching functionality using Redis.
 */
@Component
public class RedisCacheAdapter implements CachePort<ExampleStatus> {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAdapter.class);
    private static final String CACHE_PREFIX = "example-status:";
    private static final long TTL_MINUTES = 10;
    
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheAdapter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<ExampleStatus> get(String key) {
        try {
            String fullKey = CACHE_PREFIX + key;
            logger.debug("Getting value from cache with key: {}", fullKey);
            
            Object value = redisTemplate.opsForValue().get(fullKey);
            
            if (value instanceof ExampleStatus) {
                logger.debug("Cache hit for key: {}", fullKey);
                return Optional.of((ExampleStatus) value);
            }
            
            logger.debug("Cache miss for key: {}", fullKey);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error getting value from cache for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, ExampleStatus value) {
        try {
            String fullKey = CACHE_PREFIX + key;
            logger.debug("Putting value in cache with key: {}", fullKey);
            
            redisTemplate.opsForValue().set(fullKey, value, TTL_MINUTES, TimeUnit.MINUTES);
            
            logger.debug("Successfully cached value for key: {}", fullKey);
        } catch (Exception e) {
            logger.error("Error putting value in cache for key: {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        try {
            String fullKey = CACHE_PREFIX + key;
            logger.debug("Evicting cache for key: {}", fullKey);
            
            Boolean deleted = redisTemplate.delete(fullKey);
            
            if (Boolean.TRUE.equals(deleted)) {
                logger.debug("Successfully evicted cache for key: {}", fullKey);
            } else {
                logger.debug("No cache entry found to evict for key: {}", fullKey);
            }
        } catch (Exception e) {
            logger.error("Error evicting cache for key: {}", key, e);
        }
    }

    @Override
    public void clear() {
        try {
            logger.info("Clearing all example-status cache entries");
            
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                logger.info("Cleared {} cache entries", deleted);
            } else {
                logger.info("No cache entries to clear");
            }
        } catch (Exception e) {
            logger.error("Error clearing cache", e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            String fullKey = CACHE_PREFIX + key;
            Boolean exists = redisTemplate.hasKey(fullKey);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            logger.error("Error checking if key exists in cache: {}", key, e);
            return false;
        }
    }
}
