package com.ar.laboratory.baseapi.adapters.out.persistence;

import com.ar.laboratory.baseapi.domain.model.ExampleStatus;
import com.ar.laboratory.baseapi.domain.ports.out.ExampleRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ExampleRepositoryPort.
 * This is an output adapter that simulates persistence with static data.
 */
@Repository
public class InMemoryExampleRepository implements ExampleRepositoryPort {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryExampleRepository.class);
    
    private final Map<String, ExampleStatus> storage = new ConcurrentHashMap<>();

    public InMemoryExampleRepository() {
        initializeData();
    }

    private void initializeData() {
        logger.info("Initializing in-memory example repository with mock data");
        
        ExampleStatus status1 = ExampleStatus.builder()
                .id("1")
                .name("Service A")
                .status("RUNNING")
                .description("Primary service running normally")
                .createdAt(LocalDateTime.now().minusDays(10))
                .active(true)
                .build();

        ExampleStatus status2 = ExampleStatus.builder()
                .id("2")
                .name("Service B")
                .status("IDLE")
                .description("Secondary service in idle state")
                .createdAt(LocalDateTime.now().minusDays(5))
                .active(true)
                .build();

        ExampleStatus status3 = ExampleStatus.builder()
                .id("3")
                .name("Service C")
                .status("STOPPED")
                .description("Maintenance service currently stopped")
                .createdAt(LocalDateTime.now().minusDays(2))
                .active(false)
                .build();

        storage.put(status1.getId(), status1);
        storage.put(status2.getId(), status2);
        storage.put(status3.getId(), status3);
        
        logger.info("Initialized {} example statuses", storage.size());
    }

    @Override
    public Optional<ExampleStatus> findById(String id) {
        logger.debug("Finding example status by id: {}", id);
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ExampleStatus> findAll() {
        logger.debug("Finding all example statuses");
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<ExampleStatus> findAllActive() {
        logger.debug("Finding all active example statuses");
        return storage.values().stream()
                .filter(ExampleStatus::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public ExampleStatus save(ExampleStatus exampleStatus) {
        logger.debug("Saving example status: {}", exampleStatus.getId());
        storage.put(exampleStatus.getId(), exampleStatus);
        return exampleStatus;
    }
}
