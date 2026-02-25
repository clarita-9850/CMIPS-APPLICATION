package com.ihss.scheduler.repository;

import com.ihss.scheduler.entity.ExecutionMapping;
import com.ihss.scheduler.entity.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExecutionMappingRepository extends JpaRepository<ExecutionMapping, Long> {

    Optional<ExecutionMapping> findByTriggerId(String triggerId);

    Optional<ExecutionMapping> findByCmipsExecutionId(Long cmipsExecutionId);

    List<ExecutionMapping> findByJobDefinitionId(Long jobDefinitionId);

    Page<ExecutionMapping> findByJobDefinitionId(Long jobDefinitionId, Pageable pageable);

    List<ExecutionMapping> findByStatus(ExecutionStatus status);

    @Query("SELECT e FROM ExecutionMapping e WHERE e.status IN :statuses")
    List<ExecutionMapping> findByStatusIn(@Param("statuses") List<ExecutionStatus> statuses);

    @Query("SELECT e FROM ExecutionMapping e " +
           "WHERE e.jobDefinition.id = :jobId " +
           "AND e.status IN ('TRIGGERED', 'QUEUED', 'STARTING', 'RUNNING')")
    List<ExecutionMapping> findRunningExecutionsForJob(@Param("jobId") Long jobId);

    @Query("SELECT e FROM ExecutionMapping e " +
           "WHERE e.status IN ('TRIGGERED', 'QUEUED', 'STARTING', 'RUNNING')")
    List<ExecutionMapping> findAllRunningExecutions();

    @Query("SELECT e FROM ExecutionMapping e " +
           "WHERE e.jobDefinition.id = :jobId " +
           "ORDER BY e.triggeredAt DESC")
    Page<ExecutionMapping> findRecentExecutionsForJob(@Param("jobId") Long jobId, Pageable pageable);

    @Query("SELECT e FROM ExecutionMapping e " +
           "WHERE e.triggeredAt >= :since " +
           "ORDER BY e.triggeredAt DESC")
    List<ExecutionMapping> findExecutionsSince(@Param("since") LocalDateTime since);

    @Query("SELECT e FROM ExecutionMapping e " +
           "WHERE e.jobDefinition.id = :jobId " +
           "AND e.status = 'COMPLETED' " +
           "ORDER BY e.completedAt DESC")
    List<ExecutionMapping> findLastSuccessfulExecutions(@Param("jobId") Long jobId, Pageable pageable);

    @Query("SELECT e FROM ExecutionMapping e " +
           "JOIN FETCH e.jobDefinition j " +
           "WHERE e.triggeredAt >= :since " +
           "ORDER BY e.triggeredAt DESC")
    Page<ExecutionMapping> findRecentWithJobDetails(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT COUNT(e) FROM ExecutionMapping e " +
           "WHERE e.jobDefinition.id = :jobId AND e.status = :status")
    long countByJobIdAndStatus(@Param("jobId") Long jobId, @Param("status") ExecutionStatus status);

    @Query("SELECT e.status, COUNT(e) FROM ExecutionMapping e " +
           "WHERE e.triggeredAt >= :since " +
           "GROUP BY e.status")
    List<Object[]> countByStatusSince(@Param("since") LocalDateTime since);

    @Query("SELECT e FROM ExecutionMapping e " +
           "WHERE e.status IN ('TRIGGERED', 'QUEUED', 'STARTING', 'RUNNING') " +
           "AND e.triggeredAt < :threshold")
    List<ExecutionMapping> findStaleRunningExecutions(@Param("threshold") LocalDateTime threshold);
}
