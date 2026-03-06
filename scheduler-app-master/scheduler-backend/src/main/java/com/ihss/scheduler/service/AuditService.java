package com.ihss.scheduler.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihss.scheduler.entity.AuditAction;
import com.ihss.scheduler.entity.AuditLog;
import com.ihss.scheduler.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(
            String entityType,
            Long entityId,
            AuditAction action,
            String performedBy,
            Object previousState,
            Object newState,
            String changeSummary) {

        try {
            AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .previousState(convertToMap(previousState))
                .newState(convertToMap(newState))
                .changeSummary(changeSummary)
                .build();

            auditRepository.save(auditLog);

            log.debug("Logged audit action: {} on {} {} by {}",
                action, entityType, entityId, performedBy);

        } catch (Exception e) {
            log.error("Failed to log audit action: {} on {} {}",
                action, entityType, entityId, e);
        }
    }

    public void logActionWithRequest(
            String entityType,
            Long entityId,
            AuditAction action,
            String performedBy,
            String performedByRole,
            Object previousState,
            Object newState,
            String changeSummary,
            String ipAddress,
            String userAgent) {

        try {
            AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedBy(performedBy)
                .performedByRole(performedByRole)
                .previousState(convertToMap(previousState))
                .newState(convertToMap(newState))
                .changeSummary(changeSummary)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

            auditRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to log audit action with request details", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditHistory(String entityType, Long entityId, Pageable pageable) {
        return auditRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditByUser(String performedBy, Pageable pageable) {
        return auditRepository.findByPerformedBy(performedBy, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditByTimeRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditRepository.findByTimeRange(start, end, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> searchAudit(
            String entityType,
            AuditAction action,
            String performedBy,
            LocalDateTime since,
            Pageable pageable) {
        return auditRepository.findByFilters(entityType, action, performedBy, since, pageable);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getEntityHistory(String entityType, Long entityId) {
        return auditRepository.findHistoryForEntity(entityType, entityId);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getRecentJobOperations(LocalDateTime since, Pageable pageable) {
        return auditRepository.findRecentJobOperations(since, pageable);
    }

    private Map<String, Object> convertToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to convert object to map for audit: {}", e.getMessage());
            return null;
        }
    }
}
