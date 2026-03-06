package com.ihss.scheduler.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with the CMIPS backend application.
 * Uses Resilience4j for circuit breaking and retry logic.
 */
@Service
public class CmipsBackendClient {

    private static final Logger log = LoggerFactory.getLogger(CmipsBackendClient.class);

    private final RestTemplate restTemplate;
    private final String cmipsBaseUrl;
    private final String batchApiPath;

    public CmipsBackendClient(
            RestTemplate restTemplate,
            @Value("${cmips.backend.base-url}") String cmipsBaseUrl,
            @Value("${cmips.backend.batch-api-path}") String batchApiPath) {
        this.restTemplate = restTemplate;
        this.cmipsBaseUrl = cmipsBaseUrl;
        this.batchApiPath = batchApiPath;
    }

    @CircuitBreaker(name = "cmipsBackend", fallbackMethod = "triggerJobFallback")
    @Retry(name = "cmipsBackend")
    public void triggerJob(String jobName, String triggerId, Map<String, Object> parameters) {
        // CMIPS backend expects /api/batch/trigger/start endpoint
        String url = cmipsBaseUrl + batchApiPath + "/trigger/start";

        Map<String, Object> request = new HashMap<>();
        request.put("jobName", jobName);
        // CMIPS backend expects 'schedulerExecutionId' but we send triggerId as correlation ID
        request.put("triggerId", triggerId);
        request.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        log.info("Triggering job {} in CMIPS backend with trigger ID: {}", jobName, triggerId);

        restTemplate.postForEntity(url, entity, Void.class);

        log.info("Successfully triggered job {} in CMIPS backend", jobName);
    }

    @CircuitBreaker(name = "cmipsBackend", fallbackMethod = "stopJobFallback")
    @Retry(name = "cmipsBackend")
    public void stopJob(String triggerId) {
        String url = cmipsBaseUrl + batchApiPath + "/stop/" + triggerId;

        log.info("Stopping job with trigger ID: {}", triggerId);

        restTemplate.postForEntity(url, null, Void.class);

        log.info("Successfully stopped job with trigger ID: {}", triggerId);
    }

    @CircuitBreaker(name = "cmipsBackend", fallbackMethod = "restartJobFallback")
    @Retry(name = "cmipsBackend")
    public void restartJob(String triggerId) {
        String url = cmipsBaseUrl + batchApiPath + "/restart/" + triggerId;

        log.info("Restarting job with trigger ID: {}", triggerId);

        restTemplate.postForEntity(url, null, Void.class);

        log.info("Successfully restarted job with trigger ID: {}", triggerId);
    }

    @CircuitBreaker(name = "cmipsBackend")
    public boolean healthCheck() {
        try {
            String url = cmipsBaseUrl + "/actuator/health";
            restTemplate.getForEntity(url, String.class);
            return true;
        } catch (Exception e) {
            log.warn("CMIPS backend health check failed: {}", e.getMessage());
            return false;
        }
    }

    @CircuitBreaker(name = "cmipsBackend")
    public Map<String, Object> getJobStatus(String triggerId) {
        String url = cmipsBaseUrl + batchApiPath + "/status/" + triggerId;

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        return response;
    }

    // Fallback methods
    @SuppressWarnings("unused")
    private void triggerJobFallback(String jobName, String triggerId, Map<String, Object> parameters, Throwable t) {
        log.error("Failed to trigger job {} after retries: {}", jobName, t.getMessage());
        throw new RuntimeException("CMIPS backend unavailable. Job trigger failed for: " + jobName, t);
    }

    @SuppressWarnings("unused")
    private void stopJobFallback(String triggerId, Throwable t) {
        log.error("Failed to stop job {} after retries: {}", triggerId, t.getMessage());
        throw new RuntimeException("CMIPS backend unavailable. Job stop failed for trigger: " + triggerId, t);
    }

    @SuppressWarnings("unused")
    private void restartJobFallback(String triggerId, Throwable t) {
        log.error("Failed to restart job {} after retries: {}", triggerId, t.getMessage());
        throw new RuntimeException("CMIPS backend unavailable. Job restart failed for trigger: " + triggerId, t);
    }
}
