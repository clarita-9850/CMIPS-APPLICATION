package com.cmips.repository;

import com.cmips.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

    List<TaskHistory> findByTaskIdOrderByPerformedAtDesc(Long taskId);

    List<TaskHistory> findByTaskId(Long taskId);

    List<TaskHistory> findByPerformedBy(String performedBy);

    List<TaskHistory> findByTaskIdAndAction(Long taskId, TaskHistory.TaskAction action);
}
