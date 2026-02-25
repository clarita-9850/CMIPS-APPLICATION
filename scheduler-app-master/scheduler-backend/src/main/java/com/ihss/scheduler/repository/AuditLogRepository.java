package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.AuditAction;
import com.ihss.scheduler.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.actionTimestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY a.actionTimestamp DESC")
    Page<AuditLog> findByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );

    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.entityType = :entityType " +
           "AND a.entityId = :entityId " +
           "ORDER BY a.actionTimestamp DESC")
    List<AuditLog> findHistoryForEntity(
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId
    );

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:entityType IS NULL OR a.entityType = :entityType) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "AND (:performedBy IS NULL OR a.performedBy = :performedBy) " +
           "AND a.actionTimestamp >= :since " +
           "ORDER BY a.actionTimestamp DESC")
    Page<AuditLog> findByFilters(
        @Param("entityType") String entityType,
        @Param("action") AuditAction action,
        @Param("performedBy") String performedBy,
        @Param("since") LocalDateTime since,
        Pageable pageable
    );

    @Query("SELECT a.performedBy, COUNT(a) FROM AuditLog a " +
           "WHERE a.actionTimestamp >= :since " +
           "GROUP BY a.performedBy " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countActionsByUser(@Param("since") LocalDateTime since);

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a " +
           "WHERE a.actionTimestamp >= :since " +
           "GROUP BY a.action")
    List<Object[]> countByAction(@Param("since") LocalDateTime since);

    @Query("SELECT a FROM AuditLog a " +
           "WHERE a.entityType = 'JOB_DEFINITION' " +
           "AND a.action IN ('TRIGGER', 'STOP', 'HOLD', 'RESUME') " +
           "AND a.actionTimestamp >= :since " +
           "ORDER BY a.actionTimestamp DESC")
    Page<AuditLog> findRecentJobOperations(@Param("since") LocalDateTime since, Pageable pageable);
}
