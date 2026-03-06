package com.cmips.repository;

import com.cmips.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedTo(String assignedTo);

    List<Task> findByStatus(Task.TaskStatus status);

    List<Task> findByAssignedToAndStatus(String assignedTo, Task.TaskStatus status);

    Long countByAssignedToAndStatus(String assignedTo, Task.TaskStatus status);

    // Work Queue methods
    List<Task> findByWorkQueue(String workQueue);

    List<Task> findByWorkQueueAndStatus(String workQueue, Task.TaskStatus status);

    List<Task> findByQueueRole(String queueRole);

    // New: reserved/deferred queries
    List<Task> findByReservedBy(String reservedBy);

    List<Task> findByReservedByAndStatus(String reservedBy, Task.TaskStatus status);

    List<Task> findByDeferredBy(String deferredBy);

    List<Task> findByDeferredByAndStatus(String deferredBy, Task.TaskStatus status);

    // Escalation queries: overdue tasks
    @Query("SELECT t FROM Task t WHERE t.status = :status AND t.dueDate IS NOT NULL AND t.dueDate < :now")
    List<Task> findOverdueTasks(@Param("status") Task.TaskStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.status = 'OPEN' AND t.dueDate IS NOT NULL AND t.dueDate < :now")
    List<Task> findOverdueOpenTasks(@Param("now") LocalDateTime now);

    @Query("SELECT t FROM Task t WHERE t.status = 'RESERVED' AND t.dueDate IS NOT NULL AND t.dueDate < :now")
    List<Task> findOverdueReservedTasks(@Param("now") LocalDateTime now);

    // Deferred restart queries
    @Query("SELECT t FROM Task t WHERE t.status = 'DEFERRED' AND t.restartDate IS NOT NULL AND t.restartDate <= :now")
    List<Task> findDeferredTasksReadyToRestart(@Param("now") LocalDateTime now);

    // Work queue tasks by ID (queue stored as name, look up by queue ID's name)
    @Query("SELECT t FROM Task t WHERE t.workQueue = :queueName AND t.status = 'OPEN' ORDER BY t.priority ASC, t.createdAt ASC")
    List<Task> findOpenTasksInQueueByPriority(@Param("queueName") String queueName);

    // Search queries
    List<Task> findByCaseNumber(String caseNumber);

    @Query("SELECT t FROM Task t WHERE (t.subject IS NOT NULL AND LOWER(t.subject) LIKE LOWER(CONCAT(CONCAT('%', :keyword), '%'))) OR (t.title IS NOT NULL AND LOWER(t.title) LIKE LOWER(CONCAT(CONCAT('%', :keyword), '%')))")
    List<Task> searchByKeyword(@Param("keyword") String keyword);

    // Work queue task count
    Long countByWorkQueueAndStatus(String workQueue, Task.TaskStatus status);

    // Tasks by task type
    List<Task> findByTaskTypeCode(String taskTypeCode);

    // Auto-close eligible tasks
    @Query("SELECT t FROM Task t WHERE t.isNotification = false AND t.autoCloseOn IS NOT NULL AND t.status IN ('OPEN', 'RESERVED')")
    List<Task> findAutoCloseEligibleTasks();
}
