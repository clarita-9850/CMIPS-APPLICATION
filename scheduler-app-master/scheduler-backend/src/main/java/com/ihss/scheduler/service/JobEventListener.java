package com.ihss.scheduler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihss.scheduler.config.RedisConfig;
import com.ihss.scheduler.dto.JobEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Listens for job events from CMIPS backend via Redis pub/sub.
 */
@Component
public class JobEventListener implements RedisConfig.JobEventListenerDelegate {

    private static final Logger log = LoggerFactory.getLogger(JobEventListener.class);

    private final ExecutionService executionService;
    private final ObjectMapper objectMapper;

    public JobEventListener(ExecutionService executionService, ObjectMapper objectMapper) {
        this.executionService = executionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handleMessage(String message, String channel) {
        log.debug("Received message on channel {}: {}", channel, message);

        try {
            JobEventDTO event = objectMapper.readValue(message, JobEventDTO.class);
            executionService.handleJobEvent(event);
        } catch (Exception e) {
            log.error("Failed to process job event from channel {}: {}", channel, e.getMessage(), e);
        }
    }
}
